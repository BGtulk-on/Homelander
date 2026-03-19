import { useState, useEffect, useRef, useLayoutEffect } from 'react'
import gsap from 'gsap'
import './RoomsTab.css'

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

function EquipmentItem({ item, isExpanded, onToggle }) {
  const detailsRef = useRef(null)

  useEffect(() => {
    if (detailsRef.current) {
      if (isExpanded) {
        gsap.to(detailsRef.current, { height: 'auto', opacity: 1, duration: 0.3, ease: 'power2.out' })
      } else {
        gsap.to(detailsRef.current, { height: 0, opacity: 0, duration: 0.3, ease: 'power2.in' })
      }
    }
  }, [isExpanded])

  return (
    <>
      <tr className={`equipment-row ${isExpanded ? 'active' : ''}`} onClick={onToggle}>
        <td className="eq-type">{TYPE_NAMES[item.typeId] || 'misc'}</td>
        <td className="eq-model">{item.name}</td>
        <td className="eq-status">{item.status}</td>
      </tr>
      <tr>
        <td colSpan="3" style={{ padding: 0 }}>
          <div ref={detailsRef} className="equipment-details" style={{ height: 0, opacity: 0, overflow: 'hidden' }}>
            <div className="details-content">
              <div className="detail-item"><strong>Serial:</strong> {item.serialNumber}</div>
              <div className="detail-item"><strong>Condition:</strong> {(item.currentCondition || 'Unknown').replace('_', ' ')}</div>
              <div className="detail-item"><strong>Assigned To:</strong> {item.assignedTo || 'Unassigned'}</div>
              <div className="detail-item"><strong>Assigned:</strong> {item.assigned ? 'Yes' : 'No'}</div>
            </div>
          </div>
        </td>
      </tr>
    </>
  )
}

function RoomCard({ room, isExpanded, onToggle, expandedEquipmentIds, onToggleEquipment }) {
  const contentRef = useRef(null)
  const containerRef = useRef(null)

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

  return (
    <div 
      className={`room-card ${isExpanded ? 'expanded' : ''}`}
      ref={containerRef}
    >
      <div className="room-card-header" onClick={() => onToggle(room.id)}>
        <div className="room-label">
          <span className="room-text">room</span>
          <span className="room-number">{room.name}</span>
        </div>
        <div className="room-stats">
          {room.items.length} equipments
        </div>
      </div>

      <div 
        className={`room-card-content ${room.items.length === 0 ? 'empty' : ''}`}
        ref={contentRef}
        style={{ overflow: 'hidden' }}
      >
        {room.items.length > 0 ? (
          <table className="equipment-table">
            <tbody>
              {room.items.map((item) => (
                <EquipmentItem 
                  key={item.id} 
                  item={item} 
                  isExpanded={expandedEquipmentIds.includes(item.id)}
                  onToggle={() => onToggleEquipment(item.id)}
                />
              ))}
            </tbody>
          </table>
        ) : (
          <div className="placeholder-row">
            <span className="eq-type">room</span>
            <span className="eq-model">{room.name} ....</span>
            <span className="eq-status">0 ....</span>
          </div>
        )}
      </div>
    </div>
  )
}

export default function RoomsTab({ refreshTrigger }) {
  const [rooms, setRooms] = useState([])
  const [expandedRoomId, setExpandedRoomId] = useState(null)
  const [expandedEquipmentIds, setExpandedEquipmentIds] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  
  const containerRef = useRef(null)
  const hasAnimatedInRef = useRef(false)

  useEffect(() => {
    const fetchEquipment = async () => {
      try {
        if (!hasAnimatedInRef.current) {
          setIsLoading(true)
        }
        const res = await fetch('/api/equipment')
        if (!res.ok) throw new Error('Failed to fetch rooms data')
        const data = await res.json()
        
        // Group by locationId
        const groups = {
          1: { id: 1, name: '1', items: [] },
          2: { id: 2, name: '2', items: [] },
          3: { id: 3, name: '3', items: [] },
          4: { id: 4, name: '4', items: [] }
        }
        
        data.forEach(item => {
          const locId = item.locationId || 1 // Fallback to room 1 if missing
          if (groups[locId]) {
            groups[locId].items.push(item)
          }
        })
        
        setRooms(Object.values(groups))
      } catch(err) {
        setError(err.message)
      } finally {
        setIsLoading(false)
      }
    }
    
    fetchEquipment()
  }, [refreshTrigger])

  useLayoutEffect(() => {
    if (!isLoading && rooms.length > 0 && containerRef.current && !hasAnimatedInRef.current) {
      hasAnimatedInRef.current = true
      gsap.fromTo(containerRef.current.querySelectorAll('.room-card'),
        { opacity: 0, y: 20 },
        { opacity: 1, y: 0, duration: 0.6, stagger: 0.1, ease: 'power3.out' }
      )
    }
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
      <h1 className="rooms-title">Rooms</h1>
      
      <div className="rooms-list">
        {rooms.map(room => (
          <RoomCard 
            key={room.id}
            room={room}
            isExpanded={expandedRoomId === room.id}
            onToggle={toggleRoom}
            expandedEquipmentIds={expandedEquipmentIds}
            onToggleEquipment={toggleEquipment}
          />
        ))}
      </div>
    </div>
  )
}
