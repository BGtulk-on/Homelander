import { useState, useEffect, useRef, useLayoutEffect } from 'react'
import gsap from 'gsap'
import ZoomableImage from './ZoomableImage'
import LazyItem from './LazyItem'
import LazyTbody from './LazyTbody'
import './RoomsTab.css'
import { sanitizeInput } from '../utils/sanitizer'

const TYPE_NAMES = {
  1: 'laptop',
  2: 'projector',
  3: 'camera',
  4: 'tablet'
}

const LOCATION_NAMES = {
  1: '1',
  2: '2',
  3: '3',
  4: '4'
}

function EquipmentItem({ item, index, isExpanded, onToggle, user, activeRequest, onRefresh }) {
  const detailsRef = useRef(null)
  const actionAreaRef = useRef(null)
  const initialBtnRef = useRef(null)
  const expandedFormRef = useRef(null)

  const [imgError, setImgError] = useState(false)
  const [isRequesting, setIsRequesting] = useState(false)
  const [requestSent, setRequestSent] = useState(false)
  const [showDatePicker, setShowDatePicker] = useState(false)
  const [returnDate, setReturnDate] = useState('')
  const [isReturning, setIsReturning] = useState(false)

  useEffect(() => {
    setImgError(false)
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    setReturnDate(tomorrow.toISOString().split('T')[0])
  }, [item.id])

  useEffect(() => {
    if (detailsRef.current) {
      if (isExpanded) {
        gsap.to(detailsRef.current, { height: 'auto', opacity: 1, duration: 0.3, ease: 'power2.out' })
      } else {
        gsap.to(detailsRef.current, { height: 0, opacity: 0, duration: 0.3, ease: 'power2.in' })
        setShowDatePicker(false)
      }
    }
  }, [isExpanded])

  useLayoutEffect(() => {
    if (!actionAreaRef.current || !initialBtnRef.current || !expandedFormRef.current) return

    if (showDatePicker) {
      const tl = gsap.timeline()
      tl.to(initialBtnRef.current, { opacity: 0, duration: 0.2, ease: 'power2.inOut' })
      tl.to(actionAreaRef.current, {
        height: expandedFormRef.current.offsetHeight,
        duration: 0.4,
        ease: 'power3.inOut'
      }, '-=0.1')
      tl.to(expandedFormRef.current, { opacity: 1, duration: 0.2 }, '-=0.2')
    } else {
      const tl = gsap.timeline()
      tl.to(expandedFormRef.current, { opacity: 0, duration: 0.2, ease: 'power2.inOut' })
      tl.to(actionAreaRef.current, { height: '3.4rem', duration: 0.3, ease: 'power3.inOut' }, '-=0.1')
      tl.to(initialBtnRef.current, { opacity: 1, duration: 0.2 }, '-=0.1')
    }
  }, [showDatePicker])

  const handleRequestInit = (e) => {
    e.stopPropagation()
    if (!user) {
      alert("You must be logged in to request equipment.")
      return
    }

    if (activeRequest?.requestStatus === 'APPROVED') {
      handleReturn(e)
      return
    }

    if (isButtonDisabled) return
    setShowDatePicker(true)
  }

  const handleReturn = async (e) => {
    e.stopPropagation()
    if (!activeRequest) return

    if (!window.confirm("Are you sure you want to return this item?")) return

    setIsReturning(true)
    try {
      const res = await fetch(`/api/requests/${activeRequest.id}/return?condition=EXCELLENT`, {
        method: 'PUT'
      })

      if (!res.ok) throw new Error('Failed to return item')

      if (onRefresh) onRefresh()
    } catch (err) {
      alert(err.message)
    } finally {
      setIsReturning(false)
    }
  }

  const handleRequestCancel = (e) => {
    e.stopPropagation()
    setShowDatePicker(false)
  }

  const handleRequestFinal = async (e) => {
    e.stopPropagation()
    if (!returnDate) {
      alert("Please select a return date.")
      return
    }

    const selectedDate = new Date(returnDate)
    const now = new Date()
    if (selectedDate <= now) {
      alert("Return date must be in the future.")
      return
    }

    setIsRequesting(true)
    try {
      const res = await fetch('/api/requests', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user: { id: user.id },
          equipment: { id: item.id },
          requestedStartDate: now.toISOString(),
          requestedEndDate: selectedDate.toISOString()
        })
      })

      if (!res.ok) throw new Error('Failed to send request')

      setRequestSent(true)
      setShowDatePicker(false)
      if (onRefresh) onRefresh()
    } catch (err) {
      alert(err.message)
    } finally {
      setIsRequesting(false)
    }
  }

  const hasPending = activeRequest?.requestStatus === 'PENDING'
  const isApproved = activeRequest?.requestStatus === 'APPROVED'
  const isTakenBySomeoneElse = item.assigned && !isApproved
  const isButtonDisabled = isRequesting || requestSent || hasPending || isReturning || isTakenBySomeoneElse

  let buttonText = 'Request to Take'
  if (isRequesting) buttonText = 'Requesting...'
  else if (requestSent || hasPending) buttonText = 'Pending Approval'
  else if (isApproved) buttonText = isReturning ? 'Returning...' : 'Return Item'
  else if (isTakenBySomeoneElse) buttonText = 'Currently Taken'

  useLayoutEffect(() => {
    if (!containerRef.current) return;

    if (!containerRef.current.dataset.entered) {
      containerRef.current.dataset.entered = 'true'
      gsap.fromTo(containerRef.current,
        { opacity: 0, x: -10 },
        {
          opacity: 1,
          x: 0,
          duration: 0.35,
          delay: Math.min(index * 0.03, 0.5),
          ease: 'power2.out'
        }
      )
    }
  }, [index])

  const containerRef = useRef(null)

  return (
    <>
      <tr
        className={`equipment-row ${isExpanded ? 'active' : ''}`}
        onClick={onToggle}
        ref={containerRef}
      >
        <td className="eq-type">{TYPE_NAMES[item.typeId] || 'misc'}</td>
        <td className="eq-model">{item.name}</td>
        <td className="eq-status">{isTakenBySomeoneElse ? 'Taken' : item.status}</td>
      </tr>
      <tr>
        <td colSpan="3" style={{ padding: 0 }}>
          <div ref={detailsRef} className="equipment-details" style={{ height: 0, opacity: 0, overflow: 'hidden' }}>
            <div className="details-layout">
              <div className="details-content">
                <div className="detail-item"><strong>Serial:</strong> {item.serialNumber}</div>
                <div className="detail-item"><strong>Condition:</strong> {(item.currentCondition || 'Unknown').replace('_', ' ')}</div>
                <div className="detail-item"><strong>Assigned To:</strong> {item.assignedTo || 'Unassigned'}</div>
                <div className="detail-item"><strong>Assigned:</strong> {item.assigned ? 'Yes' : 'No'}</div>

                {(item.status === 'Available' && !isTakenBySomeoneElse) || isApproved ? (
                  <div
                    className={`request-action-container ${showDatePicker ? 'expanded' : ''}`}
                    ref={actionAreaRef}
                    style={{ height: '3.4rem', overflow: 'hidden', position: 'relative' }}
                  >
                    <button
                      ref={initialBtnRef}
                      className={`request-take-btn ${(requestSent || hasPending) ? 'sent' : ''} ${isApproved ? 'approved' : ''}`}
                      onClick={handleRequestInit}
                      disabled={isButtonDisabled}
                      style={{
                        position: 'absolute', top: 0, left: 0, width: '100%',
                        zIndex: showDatePicker ? 1 : 2
                      }}
                    >
                      {buttonText}
                    </button>

                    <div
                      ref={expandedFormRef}
                      className="request-expanded-form"
                      style={{
                        opacity: 0,
                        position: 'absolute', top: 0, left: 0, width: '100%',
                        zIndex: showDatePicker ? 2 : 1,
                        pointerEvents: showDatePicker ? 'all' : 'none'
                      }}
                    >
                      <div className="date-prompt-header">
                        <label className="date-label">Return Date</label>
                        <button className="date-cancel-x" onClick={handleRequestCancel}>✕</button>
                      </div>
                      <input
                        type="date"
                        className="return-date-input"
                        value={returnDate}
                        onChange={(e) => setReturnDate(e.target.value)}
                        onClick={(e) => e.stopPropagation()}
                        min={new Date().toISOString().split('T')[0]}
                      />
                      <button
                        className="request-take-btn"
                        onClick={handleRequestFinal}
                        disabled={isRequesting}
                      >
                        {isRequesting ? 'Sending...' : 'Confirm Request'}
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="status-notice-box">
                    <span className="notice-icon">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" /></svg>
                    </span>
                    <span className="notice-text">
                      {isTakenBySomeoneElse
                        ? <>Item is currently <strong>TAKEN</strong> by {item.assignedTo} and cannot be requested.</>
                        : <>Item is currently <strong>{item.status.replace('_', ' ')}</strong> and cannot be requested.</>
                      }
                    </span>
                  </div>
                )}
              </div>
              <div className="rooms-equip-photo">
                {item.photoUrl && !imgError ? (
                  <ZoomableImage
                    src={item.photoUrl}
                    alt={item.name}
                    onError={() => setImgError(true)}
                  />
                ) : (
                  <div className="no-photo-placeholder">
                    <span>NO IMAGE</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </td>
      </tr>
    </>
  )
}

function RoomCard({ room, index, isExpanded, onToggle, expandedEquipmentIds, onToggleEquipment, user, userRequests, onRefresh }) {
  const contentRef = useRef(null)
  const containerRef = useRef(null)

  const approvedInRoom = Array.isArray(userRequests) ? userRequests.filter(req =>
    req.requestStatus === 'APPROVED' &&
    room.items.some(item => item.id === req.equipment.id)
  ).length : 0

  const [isRenaming, setIsRenaming] = useState(false)
  const [editName, setEditName] = useState(room.name)

  const handleRoomRename = async () => {
    if (!editName.trim() || editName === room.name) {
      setIsRenaming(false)
      return
    }
    try {
      const res = await fetch(`/api/locations/${room.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ roomName: editName })
      })
      if (!res.ok) throw new Error('Failed to rename')
      const updated = await res.json()
      
      onRefresh(true, updated) // Provide hinted update
      setIsRenaming(false)
    } catch (err) {
      console.error(err)
    }
  }

  const handleRoomDelete = async () => {
    if (!window.confirm(`Delete ${room.name}?`)) return
    try {
      const res = await fetch(`/api/locations/${room.id}`, { method: 'DELETE' })
      if (!res.ok) throw new Error('Failed to delete')
      onRefresh(false, null, room.id) // Hint for deletion
    } catch (err) {
      console.error(err)
    }
  }

  useLayoutEffect(() => {
    if (!contentRef.current) return

    if (!containerRef.current.dataset.initialized) {
      containerRef.current.dataset.initialized = 'true'
      gsap.set(contentRef.current, {
        height: isExpanded ? 'auto' : 0,
        opacity: isExpanded ? 1 : 0,
        borderTopColor: isExpanded ? 'var(--burnt-copper)' : 'transparent'
      })
      return
    }

    if (isExpanded) {
      gsap.to(contentRef.current, {
        height: 'auto',
        opacity: 1,
        borderTopColor: 'var(--burnt-copper)',
        duration: 0.4,
        ease: 'power2.inOut'
      })
    } else {
      gsap.to(contentRef.current, {
        height: 0,
        opacity: 0,
        borderTopColor: 'transparent',
        duration: 0.4,
        ease: 'power2.inOut'
      })
    }
  }, [isExpanded])

  useLayoutEffect(() => {
    if (containerRef.current && !containerRef.current.dataset.entered) {
      containerRef.current.dataset.entered = 'true'
      gsap.fromTo(containerRef.current,
        { opacity: 0, y: 20 },
        {
          opacity: 1,
          y: 0,
          duration: 0.5,
          delay: Math.min(index * 0.1, 0.8),
          ease: 'power3.out'
        }
      )
    }
  }, [index])

  return (
    <div
      className={`room-card ${isExpanded ? 'expanded' : ''}`}
      ref={containerRef}
    >
      <div
        className={`room-card-header ${room.items.length === 0 ? 'disabled' : ''}`}
        onClick={() => room.items.length > 0 && onToggle(room.id)}
      >
        <div className="room-label">
          {isRenaming ? (
            <input 
              autoFocus
              className="room-rename-input" 
              value={editName}
              onChange={(e) => setEditName(sanitizeInput(e.target.value))}
              onBlur={() => setIsRenaming(false)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleRoomRename()
                if (e.key === 'Escape') setIsRenaming(false)
              }}
              onClick={(e) => e.stopPropagation()}
            />
          ) : (
            <span className="room-number" onDoubleClick={() => (user?.role === 'ADMIN' || user?.role === 'SUPERUSER') && setIsRenaming(true)}>
              {room.name}
            </span>
          )}
          
          {(user?.role === 'ADMIN' || user?.role === 'SUPERUSER') && !isRenaming && (
            <div className="room-admin-actions">
              <button 
                className="room-action-link" 
                onClick={(e) => { e.stopPropagation(); setIsRenaming(true); }}
              >
                rename
              </button>
              <button 
                className="room-action-link delete" 
                onClick={(e) => { e.stopPropagation(); handleRoomDelete(); }}
              >
                delete
              </button>
            </div>
          )}

          {approvedInRoom > 0 && (
            <span className="room-approved-badge">
              ({approvedInRoom} taken)
            </span>
          )}
        </div>
        <div className="room-stats">
          {room.items.length > 0 ? `${room.items.length} equipment` : 'empty'}
        </div>
      </div>

      <div
        className={`room-card-content ${room.items.length === 0 ? 'empty' : ''}`}
        ref={contentRef}
        style={{ overflow: 'hidden' }}
      >
        {room.items.length > 0 && (
          <table className="equipment-table">
            {room.items.map((item, index) => {
              const activeRequest = Array.isArray(userRequests)
                ? userRequests.find(req =>
                  req.equipment.id === item.id &&
                  (req.requestStatus === 'PENDING' || req.requestStatus === 'APPROVED')
                )
                : null

              return (
                <LazyTbody key={item.id} estimatedHeight="48px">
                  <EquipmentItem
                    key={item.id}
                    item={item}
                    index={index}
                    isExpanded={expandedEquipmentIds.includes(item.id)}
                    onToggle={() => onToggleEquipment(item.id)}
                    user={user}
                    activeRequest={activeRequest}
                    onRefresh={onRefresh}
                  />
                </LazyTbody>
              )
            })}
          </table>
        )}
      </div>
    </div>
  )
}

export default function RoomsTab({ user, refreshTrigger }) {
  const [rooms, setRooms] = useState([])
  const [userRequests, setUserRequests] = useState([])
  const [expandedRoomId, setExpandedRoomId] = useState(null)
  const [expandedEquipmentIds, setExpandedEquipmentIds] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [newRoomName, setNewRoomName] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const containerRef = useRef(null)
  const hasAnimatedInRef = useRef(false)

  const fetchData = async (hint_isUpdate, hint_payload, hint_deleteId) => {
    if (hint_isUpdate !== undefined) {
      if (hint_deleteId) {
        setRooms(prev => prev.filter(r => r.id !== hint_deleteId))
      } else if (hint_isUpdate && hint_payload) {
        setRooms(prev => prev.map(r => r.id === hint_payload.id ? { ...r, name: hint_payload.roomName } : r))
      }
      return
    }

    try {
      if (!hasAnimatedInRef.current) {
        setIsLoading(true)
      }

      const [equipRes, locRes, requestsRes] = await Promise.all([
        fetch('/api/equipment/all'),
        fetch('/api/locations'),
        (user && user.id) ? fetch(`/api/requests/user/${user.id}`).catch(() => ({ ok: false })) : Promise.resolve(null)
      ])

      if (!equipRes.ok || !locRes.ok) throw new Error('Failed to fetch rooms data')

      const [data, locations] = await Promise.all([equipRes.json(), locRes.json()])
      let requests = []

      if (requestsRes && requestsRes.ok) {
        try {
          const json = await requestsRes.json()
          if (Array.isArray(json)) requests = json
        } catch (e) {
          console.error("Error parsing requests JSON", e)
        }
      }

      const groups = {}
      locations.forEach(loc => {
        groups[loc.id] = { id: loc.id, name: loc.roomName, items: [] }
      })

      data.forEach(item => {
        const locId = item.locationId
        if (locId && groups[locId]) {
          groups[locId].items.push(item)
        }
      })

      setRooms(Object.values(groups))
      setUserRequests(requests)
    } catch (err) {
      setError(err.message)
    } finally {
      setIsLoading(false)
    }
  }

  const handleAddRoom = async (e) => {
    e.preventDefault()
    if (!newRoomName.trim() || isSubmitting) return
    
    setIsSubmitting(true)
    try {
      const res = await fetch('/api/locations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ roomName: newRoomName })
      })
      
      if (!res.ok) throw new Error('Failed to create room')
      const created = await res.json()
      
      const newRoom = { 
        id: created.id, 
        name: created.roomName, 
        items: [] 
      }
      
      setRooms(prev => [...prev, newRoom])
      setNewRoomName('')
    } catch (err) {
      console.error(err)
    } finally {
      setIsSubmitting(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [refreshTrigger, user?.id])

  useLayoutEffect(() => {
  }, [isLoading, rooms.length])

  const toggleRoom = (id) => {
    setExpandedRoomId(expandedRoomId === id ? null : id)
  }

  const toggleEquipment = (id) => {
    setExpandedEquipmentIds(prev => {
      if (prev.includes(id)) {
        return prev.filter(eId => eId !== id)
      }
      const next = [...prev, id]
      if (next.length > 2) {
        return next.slice(1)
      }
      return next
    })
  }

  if (isLoading) return <div className="rooms-tab-container">Loading rooms...</div>
  if (error) return <div className="rooms-tab-container">Error: {error}</div>

  return (
    <div className="rooms-tab-container" ref={containerRef}>
      <div className="rooms-list">
        {rooms.map((room, index) => (
          <LazyItem key={room.id} estimatedHeight="140px">
            <RoomCard
              key={room.id}
              room={room}
              index={index}
              isExpanded={expandedRoomId === room.id}
              onToggle={toggleRoom}
              expandedEquipmentIds={expandedEquipmentIds}
              onToggleEquipment={toggleEquipment}
              user={user}
              userRequests={userRequests}
              onRefresh={fetchData}
            />
          </LazyItem>
        ))}
      </div>

      {(user?.role === 'ADMIN' || user?.role === 'SUPERUSER') && (
        <form className="add-room-form" onSubmit={handleAddRoom}>
          <input 
            type="text" 
            placeholder="new room name..." 
            value={newRoomName}
            onChange={(e) => setNewRoomName(sanitizeInput(e.target.value))}
            className="add-room-input"
            required
          />
          <button type="submit" disabled={isSubmitting} className="add-room-btn">
            {isSubmitting ? '...' : 'ADD ROOM'}
          </button>
        </form>
      )}
    </div>
  )
}

