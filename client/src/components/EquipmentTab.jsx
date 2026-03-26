import { useEffect, useState, useRef, useLayoutEffect, useMemo } from 'react'
import gsap from 'gsap'
import ZoomableImage from './ZoomableImage'
import LazyItem from './LazyItem'
import './EquipmentTab.css'
import { sanitizeInput } from '../utils/sanitizer'

function ActionConfirmBtn({ actionText, confirmText, onClick, disabled }) {
  const [isConfirming, setIsConfirming] = useState(false)
  const textContainerRef = useRef(null)
  const timeoutRef = useRef(null)

  useEffect(() => {
    return () => clearTimeout(timeoutRef.current)
  }, [])

  const handleClick = (e) => {
    e.stopPropagation()
    if (disabled) return
    if (!isConfirming) {
      setIsConfirming(true)
      gsap.to(textContainerRef.current, { y: '-50%', duration: 0.25, ease: 'power2.out' })
      
      timeoutRef.current = setTimeout(() => {
        setIsConfirming(false)
        gsap.to(textContainerRef.current, { y: '0%', duration: 0.25, ease: 'power2.out' })
      }, 3000)
    } else {
      clearTimeout(timeoutRef.current)
      onClick()
    }
  }

  return (
    <button className="equip-tab-action-btn" onClick={handleClick} disabled={disabled}>
      <div style={{ height: '1.2rem', overflow: 'hidden', position: 'relative' }}>
        <div 
          ref={textContainerRef} 
          style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', height: '2.4rem' }}
        >
          <span style={{ flex: '0 0 1.2rem', display: 'flex', alignItems: 'center' }}>
            {actionText}
          </span>
          <span style={{ flex: '0 0 1.2rem', display: 'flex', alignItems: 'center', opacity: 0.8 }}>
            {confirmText}
          </span>
        </div>
      </div>
    </button>
  )
}

function EquipmentItem({ item, index, isExpanded, onToggle, onDelete, onUpdate, onAssign, users, locations, currentUser }) {
  const bodyRef = useRef(null)
  const containerRef = useRef(null)
  const actionsRef = useRef(null)
  const photoUrlRef = useRef(null)
  const [isRemoving, setIsRemoving] = useState(false)
  const [imgError, setImgError] = useState(false)
  const [isEditing, setIsEditing] = useState(false)
  const [editedItem, setEditedItem] = useState({ ...item })
  
  useEffect(() => {
    setImgError(false)
  }, [editedItem.photoUrl, item.photoUrl])
  
  const [showSuggestions, setShowSuggestions] = useState(false)
  const suggestionRef = useRef(null)

  const getTypeName = (id) => {
    const types = { 1: 'Laptop', 2: 'Projector', 3: 'Camera', 4: 'Tablet' }
    return types[id] || 'Other'
  }

  const getLocationName = (id) => {
    const loc = locations.find(l => l.id === id)
    return loc ? loc.roomName : 'Storage'
  }

  const filteredUsers = useMemo(() => {
    const query = (editedItem.assignedTo || '').toLowerCase()
    if (!query || !showSuggestions) return []
    return users.filter(u => 
      `${u.firstName} ${u.lastName}`.toLowerCase().includes(query) ||
      u.email.toLowerCase().includes(query)
    ).slice(0, 5)
  }, [editedItem.assignedTo, users, showSuggestions])

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (suggestionRef.current && !suggestionRef.current.contains(e.target)) {
        setShowSuggestions(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useLayoutEffect(() => {
    if (!bodyRef.current) return
    
    if (!containerRef.current.dataset.initialized) {
      containerRef.current.dataset.initialized = 'true'
      gsap.set(bodyRef.current, {
        height: isExpanded ? 'auto' : 0,
        opacity: isExpanded ? 1 : 0,
        paddingTop: isExpanded ? '4px' : 0,
        paddingBottom: isExpanded ? '8px' : 0,
        borderTopColor: isExpanded ? 'rgba(160, 67, 10, 0.4)' : 'transparent'
      })
      return
    }

    if (isExpanded) {
      gsap.to(bodyRef.current, {
        height: 'auto',
        opacity: 1,
        paddingTop: '4px',
        paddingBottom: '8px',
        borderTopColor: 'rgba(160, 67, 10, 0.4)',
        duration: 0.3,
        ease: 'power2.inOut'
      })
    } else {
      setIsEditing(false)
      setShowSuggestions(false)
      gsap.to(bodyRef.current, {
        height: 0,
        opacity: 0,
        paddingTop: 0,
        paddingBottom: 0,
        borderTopColor: 'transparent',
        duration: 0.3,
        ease: 'power2.inOut'
      })
    }
  }, [isExpanded])

  useLayoutEffect(() => {
    if (containerRef.current && !containerRef.current.dataset.entered) {
      containerRef.current.dataset.entered = 'true'
      gsap.fromTo(containerRef.current, 
        { opacity: 0, y: 15 },
        { 
          opacity: 1, 
          y: 0, 
          duration: 0.45, 
          delay: Math.min(index * 0.05, 0.8),
          ease: 'power2.out' 
        }
      )
    }
  }, [index])

  useLayoutEffect(() => {
    if (!photoUrlRef.current || !bodyRef.current || !isExpanded) return
    
    const startHeight = bodyRef.current.getBoundingClientRect().height;
    
    if (isEditing) {
      gsap.set(photoUrlRef.current, { height: 'auto', opacity: 1 });
      const endHeight = bodyRef.current.scrollHeight;
      gsap.fromTo(photoUrlRef.current, 
        { height: 0, opacity: 0 }, 
        { height: 'auto', opacity: 1, duration: 0.3, ease: 'power2.out' }
      )
      gsap.fromTo(bodyRef.current,
        { height: startHeight },
        { height: endHeight, duration: 0.3, ease: 'power2.out', onComplete: () => {
          gsap.set(bodyRef.current, { height: 'auto' });
        }}
      )
    } else {
      const currentHeight = bodyRef.current.getBoundingClientRect().height;
      gsap.set(photoUrlRef.current, { height: 0, opacity: 0 });
      const targetHeight = bodyRef.current.scrollHeight;
      gsap.set(photoUrlRef.current, { height: 'auto', opacity: 1 });
      gsap.to(photoUrlRef.current, { height: 0, opacity: 0, duration: 0.25, ease: 'power2.in' });
      gsap.fromTo(bodyRef.current,
        { height: currentHeight },
        { height: targetHeight, duration: 0.25, ease: 'power2.in', onComplete: () => {
          gsap.set(bodyRef.current, { height: 'auto' });
        }}
      )
    }
  }, [isEditing, isExpanded])

  useLayoutEffect(() => {
    if (!actionsRef.current) return
    const editMain = actionsRef.current.querySelector('.edit-main-btn')
    const editText = editMain.querySelector('.btn-text')
    const splitRow = actionsRef.current.querySelector('.equip-tab-actions-row')
    const cancelText = splitRow.querySelector('button:first-child .btn-text')
    const saveText = splitRow.querySelector('button:last-child .btn-text')

    const tl = gsap.timeline()

    if (isEditing) {
      tl.to(editText, { opacity: 0, duration: 0.1, ease: 'power2.in' })
      tl.set(editMain, { opacity: 0, pointerEvents: 'none' })
      tl.set(splitRow, { opacity: 1, pointerEvents: 'all', gap: 0 })
      tl.set([cancelText, saveText], { opacity: 0 })
      tl.to(splitRow, { gap: '-2px', duration: 0.08, ease: 'power2.inOut' })
      tl.to(splitRow, { 
        gap: '0.4rem', 
        duration: 0.5, 
        ease: 'elastic.out(1.1, 0.6)' 
      })
      tl.to([cancelText, saveText], { opacity: 1, duration: 0.15, ease: 'power2.out' }, '-=0.3')
    } else {
      tl.to([cancelText, saveText], { opacity: 0, duration: 0.1, ease: 'power2.in' })
      tl.to(splitRow, { gap: 0, duration: 0.25, ease: 'power3.inOut' })
      tl.set(splitRow, { opacity: 0, pointerEvents: 'none' })
      tl.set(editMain, { opacity: 1, pointerEvents: 'all' })
      tl.to(editText, { opacity: 1, duration: 0.15, ease: 'power2.out' })
    }
  }, [isEditing])

  const handleDeleteWithAnim = () => {
    setIsRemoving(true)
    const tl = gsap.timeline({
      onComplete: () => onDelete(item.id)
    })
    tl.to(bodyRef.current, { height: 0, opacity: 0, duration: 0.25 })
    tl.to(containerRef.current, {
      height: 0, opacity: 0, marginBottom: 0, paddingTop: 0, paddingBottom: 0,
      borderTopWidth: 0, borderBottomWidth: 0, duration: 0.35, ease: 'power2.inOut'
    })
  }

  const handleSave = async () => {
    if (editedItem.assignedTo !== item.assignedTo) {
      const isUserValid = users.some(u => `${u.firstName} ${u.lastName}` === editedItem.assignedTo)
      if (editedItem.assignedTo && !isUserValid) {
        alert("Please select a valid user from the suggestions.")
        return
      }
      await onAssign(item.id, editedItem.assignedTo)
    }
    
    const success = await onUpdate(item.id, editedItem)
    if (success) setIsEditing(false)
  }

  const selectUser = (u) => {
    const fullName = `${u.firstName} ${u.lastName}`
    setEditedItem({ ...editedItem, assignedTo: fullName })
    setShowSuggestions(false)
  }

  return (
    <div 
      className={`equip-tab-item-box ${isExpanded ? 'expanded' : ''}`} 
      ref={containerRef}
      style={{ overflow: 'hidden' }}
    >
      <button 
        className="equip-tab-item-header"
        onClick={() => !isRemoving && onToggle(item.id)}
      >
        <div className="equip-tab-header-main">
          {isEditing ? (
            <input 
              className="edit-input" 
              value={editedItem.name} 
              onClick={e => e.stopPropagation()}
              onChange={e => setEditedItem({ ...editedItem, name: sanitizeInput(e.target.value) })}
              style={{ fontSize: '1.1rem', fontWeight: 600, padding: '2px 4px' }}
            />
          ) : (
            <span className="equip-tab-name">{item.name}</span>
          )}
          <span className="equip-tab-type-tag">{getTypeName(item.typeId)}</span>
        </div>
        <svg 
          className={`equip-tab-icon ${isExpanded ? 'expanded' : ''}`} 
          viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
        >
          <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </button>
      
      <div className="equip-tab-item-body" ref={bodyRef} style={{ overflow: 'visible' }}>
        <div className="equip-tab-body-info">
          <div className="equip-info-grid">
            <div className="equip-info-item" title="Serial Number">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M4 9h16"/><path d="M4 15h16"/><path d="M10 3L8 21"/><path d="M16 3l-2 18"/></svg>
              </span>
              {isEditing ? (
                <input className="edit-input" value={editedItem.serialNumber} onChange={e => setEditedItem({ ...editedItem, serialNumber: sanitizeInput(e.target.value) })} />
              ) : (
                <span className="info-value">{item.serialNumber}</span>
              )}
            </div>
            <div className="equip-info-item" title="Status">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              </span>
              {isEditing ? (
                <select className="edit-input" value={editedItem.status} onChange={e => setEditedItem({ ...editedItem, status: e.target.value })}>
                  <option value="Available">Available</option>
                  <option value="Checked_Out">Checked Out</option>
                  <option value="Under_Repair">Under Repair</option>
                  <option value="Retired">Retired</option>
                </select>
              ) : (
                <span className={`info-value status-${item.status.toLowerCase().replace(' ', '-')}`}>{item.status}</span>
              )}
            </div>
            <div className="equip-info-item" title="Condition">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              </span>
              {isEditing ? (
                <select className="edit-input" value={editedItem.currentCondition} onChange={e => setEditedItem({ ...editedItem, currentCondition: e.target.value })}>
                  <option value="EXCELLENT">Excellent</option>
                  <option value="VERY_GOOD">Very Good</option>
                  <option value="GOOD">Good</option>
                  <option value="POOR">Poor</option>
                </select>
              ) : (
                <span className="info-value">{(item.currentCondition || 'N/A').replace('_', ' ')}</span>
              )}
            </div>
            <div className="equip-info-item" title="Location">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              </span>
              {isEditing ? (
                <select className="edit-input" value={editedItem.locationId} onChange={e => setEditedItem({ ...editedItem, locationId: parseInt(e.target.value) })}>
                  {locations.map(loc => (
                    <option key={loc.id} value={loc.id}>{loc.roomName}</option>
                  ))}
                </select>
              ) : (
                <span className="info-value">{getLocationName(item.locationId)}</span>
              )}
            </div>
            
            <div className="equip-info-item full-width" title="Assigned To" style={{ position: 'relative' }}>
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </span>
              {isEditing ? (
                <div style={{ width: '100%', position: 'relative' }}>
                  <input 
                    className="edit-input" 
                    value={editedItem.assignedTo || ''} 
                    onChange={e => {
                      const sanitized = sanitizeInput(e.target.value)
                      setEditedItem({ ...editedItem, assignedTo: sanitized })
                      setShowSuggestions(true)
                    }}
                    onFocus={() => setShowSuggestions(true)}
                    placeholder="Type name to suggest..."
                  />
                  {showSuggestions && filteredUsers.length > 0 && (
                    <div className="user-suggestions-box" ref={suggestionRef}>
                      {filteredUsers.map(u => (
                        <div key={u.id} className="user-suggestion-item" onClick={() => selectUser(u)}>
                          <span className="suggestion-name">{u.firstName} {u.lastName}</span>
                          <span className="suggestion-email">{u.email}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ) : (
                <span className="info-value">{item.assignedTo || 'Unassigned'}</span>
              )}
            </div>
            
            <div className="photo-url-field-container" ref={photoUrlRef}>
              <div className="equip-info-item full-width" title="Image URL">
                <span className="info-icon">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
                </span>
                <input className="edit-input" value={editedItem.photoUrl || ''} onChange={e => setEditedItem({ ...editedItem, photoUrl: sanitizeInput(e.target.value) })} placeholder="Image URL" />
              </div>
            </div>
          </div>
          
          {(item.photoUrl || editedItem.photoUrl) && !imgError && (
            <div className="equip-photo-preview">
              <ZoomableImage 
                src={isEditing ? editedItem.photoUrl : item.photoUrl} 
                alt={item.name} 
                onError={() => setImgError(true)}
              />
            </div>
          )}
          </div>

          <div className="equip-tab-actions" ref={actionsRef}>
            <div style={{ padding: '0 0.5rem', marginBottom: '0.8rem', display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
              <span style={{ fontSize: '0.65rem', fontWeight: 'bold', color: '#A0430A', opacity: 0.8 }}>HISTORY REPORT</span>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button 
                  className="users-tab-action-btn export" 
                  style={{ fontSize: '0.65rem', padding: '0.2rem 0.5rem', borderStyle: 'dashed', flex: 1, justifyContent: 'center' }}
                  onClick={(e) => { e.stopPropagation(); window.location.href=`/reports/equipment/${item.id}/export?requestingUserId=${currentUser.id}&format=csv` }}
                >
                  CSV
                </button>
                <button 
                  className="users-tab-action-btn export" 
                  style={{ fontSize: '0.65rem', padding: '0.2rem 0.5rem', borderStyle: 'dashed', flex: 1, justifyContent: 'center' }}
                  onClick={(e) => { e.stopPropagation(); window.location.href=`/reports/equipment/${item.id}/export?requestingUserId=${currentUser.id}&format=pdf` }}
                >
                  PDF
                </button>
              </div>
            </div>

            <div className="edit-logic-container">
            <button className="equip-tab-action-btn edit-main-btn" onClick={() => setIsEditing(true)} disabled={isRemoving}>
              <span className="btn-text">Edit</span>
            </button>
            <div className="equip-tab-actions-row" style={{ opacity: 0, pointerEvents: 'none' }}>
              <button className="equip-tab-action-btn" onClick={() => { setIsEditing(false); setEditedItem({ ...item }); setShowSuggestions(false); }}>
                <span className="btn-text">Cancel</span>
              </button>
              <button className="equip-tab-action-btn" style={{ background: '#A0430A', color: '#DFE8E6' }} onClick={handleSave}>
                <span className="btn-text">Save</span>
              </button>
            </div>
          </div>
          <ActionConfirmBtn actionText="Delete" confirmText="Confirm?" onClick={handleDeleteWithAnim} disabled={isRemoving} />
        </div>
      </div>
    </div>
  )
}

export default function EquipmentTab({ onEquipmentChange, currentUser }) {
  const [equipment, setEquipment] = useState([])
  const [users, setUsers] = useState([])
  const [locations, setLocations] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [expandedId, setExpandedId] = useState(null)
  const [showAddForm, setShowAddForm] = useState(false)
  
  const [newEquip, setNewEquip] = useState({
    name: '', serialNumber: '', typeId: 1, locationId: 1, currentCondition: 'EXCELLENT', status: 'Available', assignedTo: '', photoUrl: ''
  })

  const formRef = useRef(null)
  const listRef = useRef(null)
  const initialLoadRef = useRef(true)

  const fetchData = async (query = '') => {
    try {
      setIsLoading(true)
      const [equipRes, userRes, locRes] = await Promise.all([
        query 
          ? fetch('/api/equipment/search', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ query })
            })
          : fetch('/api/equipment/all'),
        fetch('/api/users'),
        fetch('/api/locations')
      ])
      
      if (!equipRes.ok || !userRes.ok || !locRes.ok) throw new Error('Failed to fetch data')
      
      const [equipData, userData, locData] = await Promise.all([equipRes.json(), userRes.json(), locRes.json()])
      setEquipment(equipData)
      setUsers(userData)
      setLocations(locData)
    } catch (err) {
      setError(err.message)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchData(searchQuery)
    }, 400)
    return () => clearTimeout(timer)
  }, [searchQuery])

  useLayoutEffect(() => {
    if (formRef.current) {
      if (showAddForm) {
        gsap.to(formRef.current, { height: 'auto', opacity: 1, paddingTop: '1rem', paddingBottom: '1rem', duration: 0.4, ease: 'power2.out' })
      } else {
        gsap.to(formRef.current, { height: 0, opacity: 0, paddingTop: 0, paddingBottom: 0, duration: 0.3, ease: 'power2.in' })
      }
    }
  }, [showAddForm])

  useLayoutEffect(() => {
    // Entrance handled by items
  }, [isLoading, equipment])

  const handleAdd = async (e) => {
    e.preventDefault()
    try {
      const res = await fetch('/api/equipment', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newEquip)
      })
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}))
        throw new Error(errorData.message || 'Failed to add equipment (URL might be too long)')
      }
      const created = await res.json()
      setEquipment(prev => [created, ...prev])
      setShowAddForm(false)
      setNewEquip({ name: '', serialNumber: '', typeId: 1, locationId: 1, currentCondition: 'EXCELLENT', status: 'Available', assignedTo: '', photoUrl: '' })
      if (onEquipmentChange) onEquipmentChange()
    } catch (err) { alert(err.message) }
  }

  const handleDelete = async (id) => {
    try {
      const res = await fetch(`/api/equipment/${id}`, { method: 'DELETE' })
      if (!res.ok) throw new Error('Failed to delete')
      setEquipment(prev => prev.filter(e => e.id !== id))
      if (onEquipmentChange) onEquipmentChange()
    } catch (err) { alert(err.message) }
  }

  const handleUpdate = async (id, details) => {
    try {
      const res = await fetch(`/api/equipment/${id}`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(details)
      })
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}))
        throw new Error(errorData.message || 'Failed to update equipment (URL might be too long)')
      }
      const updated = await res.json()
      setEquipment(prev => prev.map(e => e.id === id ? updated : e))
      if (onEquipmentChange) onEquipmentChange()
      return true
    } catch (err) { alert(err.message); return false }
  }

  const handleAssign = async (id, personName) => {
    try {
      const res = await fetch(`/api/equipment/${id}/assign?personName=${encodeURIComponent(personName)}`, {
        method: 'PATCH'
      })
      if (!res.ok) throw new Error('Failed to assign equipment')
      const updated = await res.json()
      setEquipment(prev => prev.map(e => e.id === id ? updated : e))
      if (onEquipmentChange) onEquipmentChange()
      return true
    } catch (err) { alert(err.message); return false }
  }

  return (
    <div className="equip-tab-container">
      <div className="equip-tab-search-wrapper">
        <input type="text" className="equip-tab-search" placeholder="search equipment" value={searchQuery} onChange={(e) => setSearchQuery(sanitizeInput(e.target.value))} />
      </div>

      <div style={{ padding: '0 0.5rem', marginBottom: '1rem', display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
        <span style={{ fontSize: '0.65rem', fontWeight: 'bold', color: '#A0430A', opacity: 0.8 }}>INVENTORY REPORT (ALL)</span>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button 
            className="users-tab-action-btn export" 
            style={{ fontSize: '0.7rem', padding: '0.3rem 0.6rem', borderStyle: 'dashed', flex: 1, justifyContent: 'center' }}
            onClick={() => window.location.href=`/reports/equipment/all/export?requestingUserId=${currentUser.id}&format=csv`}
          >
            CSV
          </button>
          <button 
            className="users-tab-action-btn export" 
            style={{ fontSize: '0.7rem', padding: '0.3rem 0.6rem', borderStyle: 'dashed', flex: 1, justifyContent: 'center' }}
            onClick={() => window.location.href=`/reports/equipment/all/export?requestingUserId=${currentUser.id}&format=pdf`}
          >
            PDF
          </button>
        </div>
      </div>

      <div className="equip-tab-add-wrapper">
        <button className={`equip-tab-add-toggle ${showAddForm ? 'active' : ''}`} onClick={() => setShowAddForm(!showAddForm)}>
          <span>Add New Equipment</span><span className="plus-icon">{showAddForm ? '−' : '+'}</span>
        </button>
        <div ref={formRef} className="equip-tab-add-form-container" style={{ height: 0, opacity: 0, overflow: 'hidden' }}>
          <form className="equip-tab-add-form" onSubmit={handleAdd}>
            <input type="text" placeholder="Name" value={newEquip.name} onChange={e => setNewEquip({...newEquip, name: sanitizeInput(e.target.value)})} required />
            <input type="text" placeholder="Serial Number" value={newEquip.serialNumber} onChange={e => setNewEquip({...newEquip, serialNumber: sanitizeInput(e.target.value)})} required />
            <div className="equip-tab-form-row">
              <select value={newEquip.typeId} onChange={e => setNewEquip({...newEquip, typeId: parseInt(e.target.value)})}>
                <option value={1}>Laptop</option><option value={2}>Projector</option><option value={3}>Camera</option><option value={4}>Tablet</option>
              </select>
              <select value={newEquip.locationId} onChange={e => setNewEquip({...newEquip, locationId: parseInt(e.target.value)})}>
                {locations.map(loc => (
                  <option key={loc.id} value={loc.id}>{loc.roomName}</option>
                ))}
              </select>
            </div>
            <div className="equip-tab-form-row">
              <select value={newEquip.currentCondition} onChange={e => setNewEquip({...newEquip, currentCondition: e.target.value})}>
                <option value="EXCELLENT">Excellent</option><option value="VERY_GOOD">Very Good</option><option value="GOOD">Good</option><option value="POOR">Poor</option>
              </select>
              <select value={newEquip.status} onChange={e => setNewEquip({...newEquip, status: e.target.value})}>
                <option value="Available">Available</option><option value="Checked_Out">Checked Out</option><option value="Under_Repair">Under Repair</option><option value="Retired">Retired</option>
              </select>
            </div>
            <input type="text" placeholder="Assign To (Optional)" value={newEquip.assignedTo} onChange={e => setNewEquip({...newEquip, assignedTo: sanitizeInput(e.target.value)})} />
            <input type="text" placeholder="Photo URL (Optional)" value={newEquip.photoUrl} onChange={e => setNewEquip({...newEquip, photoUrl: sanitizeInput(e.target.value)})} />
            <button type="submit" className="equip-tab-submit-btn">Save Equipment</button>
          </form>
        </div>
      </div>
      
      {isLoading && <div className="equip-tab-loading">Loading...</div>}
      {error && <div className="equip-tab-error">Error: {error}</div>}
      
      {!isLoading && !error && (
        <div className="equip-tab-list" ref={listRef}>
          {equipment.length > 0 ? equipment.map((item, index) => (
            <LazyItem key={item.id} estimatedHeight="60px">
              <EquipmentItem 
                item={item} 
                index={index}
                isExpanded={expandedId === item.id} 
                onToggle={id => setExpandedId(expandedId === id ? null : id)}
                onDelete={handleDelete} onUpdate={handleUpdate} onAssign={handleAssign} users={users} locations={locations}
                currentUser={currentUser}
              />
            </LazyItem>
          )) : (
            <div style={{ textAlign: 'center', padding: '2rem', color: '#A0430A', opacity: 0.6 }}>No equipment found</div>
          )}
        </div>
      )}
    </div>
  )
}