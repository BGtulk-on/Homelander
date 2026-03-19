import { useEffect, useState, useRef, useLayoutEffect } from 'react'
import gsap from 'gsap'
import './EquipmentTab.css'

function EquipmentItem({ item, isExpanded, onToggle, onDelete }) {
  const bodyRef = useRef(null)
  const containerRef = useRef(null)
  const [isRemoving, setIsRemoving] = useState(false)

  useLayoutEffect(() => {
    if (!bodyRef.current) return
    
    if (!containerRef.current.dataset.initialized) {
      containerRef.current.dataset.initialized = 'true'
      gsap.set(bodyRef.current, {
        height: isExpanded ? 'auto' : 0,
        opacity: isExpanded ? 1 : 0,
        paddingTop: isExpanded ? '2px' : 0,
        paddingBottom: isExpanded ? '6px' : 0,
        paddingLeft: isExpanded ? '4px' : 0,
        paddingRight: isExpanded ? '4px' : 0,
        borderTopColor: isExpanded ? 'rgba(160, 67, 10, 0.4)' : 'transparent'
      })
      return
    }

    if (isExpanded) {
      gsap.to(bodyRef.current, {
        height: 'auto',
        opacity: 1,
        paddingTop: '2px',
        paddingBottom: '6px',
        paddingLeft: '4px',
        paddingRight: '4px',
        borderTopColor: 'rgba(160, 67, 10, 0.4)',
        duration: 0.3,
        ease: 'power2.inOut'
      })
    } else {
      gsap.to(bodyRef.current, {
        height: 0,
        opacity: 0,
        paddingTop: 0,
        paddingBottom: 0,
        paddingLeft: 0,
        paddingRight: 0,
        borderTopColor: 'transparent',
        duration: 0.3,
        ease: 'power2.inOut'
      })
    }
  }, [isExpanded])

  const handleDeleteWithAnim = () => {
    setIsRemoving(true)
    const tl = gsap.timeline({
      onComplete: () => onDelete(item.id)
    })
    
    tl.to(bodyRef.current, { height: 0, opacity: 0, duration: 0.25 })
    tl.to(containerRef.current, {
      height: 0,
      opacity: 0,
      marginBottom: 0,
      paddingTop: 0,
      paddingBottom: 0,
      borderTopWidth: 0,
      borderBottomWidth: 0,
      duration: 0.35,
      ease: 'power2.inOut'
    })
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
        <span className="equip-tab-name">
          {item.name}
        </span>
        <svg 
          className={`equip-tab-icon ${isExpanded ? 'expanded' : ''}`} 
          viewBox="0 0 24 24" 
          fill="none" 
          xmlns="http://www.w3.org/2000/svg"
        >
          <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </button>
      
      <div 
        className="equip-tab-item-body"
        ref={bodyRef}
        style={{ overflow: 'hidden' }}
      >
        <div className="equip-tab-body-info">
          <p><strong>SN:</strong> {item.serialNumber}</p>
          <p><strong>Status:</strong> {item.status}</p>
          <p><strong>Condition:</strong> {item.currentCondition || 'Unknown'}</p>
        </div>
        
        <div className="equip-tab-actions">
          <button 
            className="equip-tab-action-btn delete" 
            onClick={handleDeleteWithAnim}
            disabled={isRemoving}
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  )
}

export default function EquipmentTab({ onEquipmentChange }) {
  const [equipment, setEquipment] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [expandedId, setExpandedId] = useState(null)
  const [showAddForm, setShowAddForm] = useState(false)
  
  const [newEquip, setNewEquip] = useState({
    name: '',
    serialNumber: '',
    typeId: 1,
    locationId: 1,
    currentCondition: 'EXCELLENT',
    status: 'Available',
    assignedTo: '',
    photoUrl: ''
  })

  const formRef = useRef(null)
  const listRef = useRef(null)
  const hasAnimatedInRef = useRef(false)

  const fetchEquipment = async () => {
    try {
      setIsLoading(true)
      const res = await fetch('/api/equipment')
      if (!res.ok) throw new Error('Failed to fetch equipment')
      const data = await res.json()
      setEquipment(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchEquipment()
  }, [])

  useLayoutEffect(() => {
    if (formRef.current) {
      if (showAddForm) {
        gsap.to(formRef.current, { 
          height: 'auto', 
          opacity: 1, 
          paddingTop: '1rem',
          paddingBottom: '1rem',
          duration: 0.4, 
          ease: 'power2.out' 
        })
      } else {
        gsap.to(formRef.current, { 
          height: 0, 
          opacity: 0, 
          paddingTop: 0,
          paddingBottom: 0,
          duration: 0.3, 
          ease: 'power2.in' 
        })
      }
    }
  }, [showAddForm])

  useLayoutEffect(() => {
    if (!isLoading && equipment.length > 0 && listRef.current && !hasAnimatedInRef.current) {
      hasAnimatedInRef.current = true
      const items = listRef.current.querySelectorAll('.equip-tab-item-box')
      gsap.fromTo(items, 
        { opacity: 0, y: 15 },
        { opacity: 1, y: 0, duration: 0.3, stagger: 0.04, ease: 'power2.out' }
      )
    }
  }, [isLoading, equipment])

  const handleAdd = async (e) => {
    e.preventDefault()
    try {
      const res = await fetch('/api/equipment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newEquip)
      })
      if (!res.ok) throw new Error('Failed to add equipment')
      const created = await res.json()
      setEquipment(prev => [created, ...prev])
      setShowAddForm(false)
      setNewEquip({ 
        name: '', 
        serialNumber: '', 
        typeId: 1, 
        locationId: 1, 
        currentCondition: 'EXCELLENT', 
        status: 'Available', 
        assignedTo: '', 
        photoUrl: '' 
      })
      if (onEquipmentChange) onEquipmentChange()
    } catch (err) {
      alert(err.message)
    }
  }

  const handleDelete = async (id) => {
    try {
      const res = await fetch(`/api/equipment/${id}`, { method: 'DELETE' })
      if (!res.ok) throw new Error('Failed to delete')
      setEquipment(prev => prev.filter(e => e.id !== id))
      if (onEquipmentChange) onEquipmentChange()
    } catch (err) {
      alert(err.message)
    }
  }

  const filteredEquipment = equipment.filter(item => 
    item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    item.serialNumber.toLowerCase().includes(searchQuery.toLowerCase())
  )

  return (
    <div className="equip-tab-container">
      <div className="equip-tab-search-wrapper">
        <input 
          type="text" 
          className="equip-tab-search" 
          placeholder="search equipment" 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      <div className="equip-tab-add-wrapper">
        <button 
          className={`equip-tab-add-toggle ${showAddForm ? 'active' : ''}`}
          onClick={() => setShowAddForm(!showAddForm)}
        >
          <span>Add New Equipment</span>
          <span className="plus-icon">{showAddForm ? '−' : '+'}</span>
        </button>
        
        <div ref={formRef} className="equip-tab-add-form-container" style={{ height: 0, opacity: 0, overflow: 'hidden' }}>
          <form className="equip-tab-add-form" onSubmit={handleAdd}>
            <input 
              type="text" 
              placeholder="Name" 
              value={newEquip.name}
              onChange={e => setNewEquip({...newEquip, name: e.target.value})}
              required
            />
            <input 
              type="text" 
              placeholder="Serial Number" 
              value={newEquip.serialNumber}
              onChange={e => setNewEquip({...newEquip, serialNumber: e.target.value})}
              required
            />
            <div className="equip-tab-form-row">
              <select 
                value={newEquip.typeId}
                onChange={e => setNewEquip({...newEquip, typeId: parseInt(e.target.value)})}
              >
                <option value={1}>Laptop</option>
                <option value={2}>Projector</option>
                <option value={3}>Camera</option>
                <option value={4}>Tablet</option>
              </select>
              <select 
                value={newEquip.locationId}
                onChange={e => setNewEquip({...newEquip, locationId: parseInt(e.target.value)})}
              >
                <option value={1}>Room 1</option>
                <option value={2}>Room 2</option>
                <option value={3}>Room 3</option>
                <option value={4}>Room 4</option>
              </select>
            </div>
            <div className="equip-tab-form-row">
              <select 
                value={newEquip.currentCondition}
                onChange={e => setNewEquip({...newEquip, currentCondition: e.target.value})}
              >
                <option value="EXCELLENT">Excellent</option>
                <option value="VERY_GOOD">Very Good</option>
                <option value="GOOD">Good</option>
                <option value="POOR">Poor</option>
              </select>
              <select 
                value={newEquip.status}
                onChange={e => setNewEquip({...newEquip, status: e.target.value})}
              >
                <option value="Available">Available</option>
                <option value="Checked_Out">Checked Out</option>
                <option value="Under_Repair">Under Repair</option>
                <option value="Retired">Retired</option>
              </select>
            </div>
            <input 
              type="text" 
              placeholder="Assign To (Optional)" 
              value={newEquip.assignedTo}
              onChange={e => setNewEquip({...newEquip, assignedTo: e.target.value})}
            />
            <input 
              type="text" 
              placeholder="Photo URL (Optional)" 
              value={newEquip.photoUrl}
              onChange={e => setNewEquip({...newEquip, photoUrl: e.target.value})}
            />
            <button type="submit" className="equip-tab-submit-btn">Save Equipment</button>
          </form>
        </div>
      </div>
      
      {isLoading && <div className="equip-tab-loading">Loading...</div>}
      {error && <div className="equip-tab-error">Error: {error}</div>}
      
      {!isLoading && !error && (
        <div className="equip-tab-list" ref={listRef}>
          {filteredEquipment.map(item => (
            <EquipmentItem 
              key={item.id} 
              item={item} 
              isExpanded={expandedId === item.id} 
              onToggle={id => setExpandedId(expandedId === id ? null : id)}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </div>
  )
}
