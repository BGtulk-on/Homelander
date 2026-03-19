import { useEffect, useState, useRef, useLayoutEffect } from 'react'
import gsap from 'gsap'
import './UsersTab.css'

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
    <button className="users-tab-action-btn" onClick={handleClick} disabled={disabled}>
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

function UserItem({ user, isExpanded, onToggle, onMakeAdmin, onApprove, onDelete }) {
  const bodyRef = useRef(null)
  const containerRef = useRef(null)
  const badgeRef = useRef(null)
  const [isRemoving, setIsRemoving] = useState(false)
  const [isPromoting, setIsPromoting] = useState(false)

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

  useEffect(() => {
    if (user.isAdmin && badgeRef.current) {
      gsap.fromTo(badgeRef.current, 
        { opacity: 0, scale: 0.8, x: 10 },
        { opacity: 1, scale: 1, x: 0, duration: 0.4, ease: 'back.out(1.7)' }
      )
    }
  }, [user.isAdmin])

  const handleLiveRemove = (callback) => {
    setIsRemoving(true)
    const tl = gsap.timeline({
        onComplete: () => callback(user.id)
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

  const handleMakeAdminWithAnim = () => {
    setIsPromoting(true)
    const btn = bodyRef.current.querySelector('.make-admin-btn-wrapper')
    if (btn) {
      gsap.to(btn, { 
        opacity: 0, 
        y: -5,
        duration: 0.25, 
        ease: 'power2.inOut', 
        onComplete: () => {
          onMakeAdmin(user.id)
          setIsPromoting(false)
        }
      })
    } else {
      onMakeAdmin(user.id)
    }
  }

  const approveWithAnim = () => handleLiveRemove(onApprove)
  const deleteWithAnim = () => handleLiveRemove(onDelete)

  return (
    <div 
      className={`users-tab-item-box ${isExpanded ? 'expanded' : ''}`} 
      ref={containerRef}
      style={{ overflow: 'hidden' }}
    >
      <button 
        className="users-tab-item-header"
        onClick={() => !isRemoving && onToggle(user.id)}
      >
        <div className="users-tab-header-main">
          <span className="users-tab-username">
            {user.firstName} {user.lastName}
          </span>
          <span className="users-tab-type-tag">
            {user.isAdmin ? 'Admin' : 'User'}
          </span>
        </div>
        <svg 
          className={`users-tab-icon ${isExpanded ? 'expanded' : ''}`} 
          viewBox="0 0 24 24" 
          fill="none" 
          xmlns="http://www.w3.org/2000/svg"
        >
          <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </button>
      
      <div 
        className="users-tab-item-body"
        ref={bodyRef}
        style={{ overflow: 'hidden' }}
      >
        <div className="users-tab-body-info">
          <div className="users-info-grid">
            <div className="users-info-item full-width" title="Role">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              </span>
              <span className="info-value">
                {user.isAdmin ? 'Administrator' : 'User'}
              </span>
            </div>

            <div className="users-info-item full-width" title="Email">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
              </span>
              <span className="info-value">{user.email}</span>
            </div>
            
            <div className="users-info-item full-width" title="Phone">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
              </span>
              <span className="info-value">{user.phone || 'N/A'}</span>
            </div>

            <div className="users-info-item" title="Address">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              </span>
              <span className="info-value">{user.address || 'N/A'}</span>
            </div>

            <div className="users-info-item" title="Joined Date">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              </span>
              <span className="info-value">{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}</span>
            </div>
          </div>
        </div>
        
        <div className="users-tab-actions">
          {!user.isAdmin && (
            <div 
              className="make-admin-btn-wrapper" 
              style={{ width: '100%', display: isPromoting ? 'none' : 'block' }}
            >
              <ActionConfirmBtn 
                key={`admin-${user.id}`}
                actionText="Make Admin"
                confirmText="Confirm?"
                onClick={handleMakeAdminWithAnim}
              />
            </div>
          )}
          
          <button 
            className="users-tab-action-btn" 
            onClick={approveWithAnim}
            disabled={isRemoving}
          >
            Approve
          </button>
          
          <ActionConfirmBtn 
            key={`delete-${user.id}`}
            actionText="Delete"
            confirmText="Confirm?"
            onClick={deleteWithAnim}
          />
        </div>
      </div>
    </div>
  )
}

export default function UsersTab() {
  const [users, setUsers] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [expandedId, setExpandedId] = useState(null)
  
  const listRef = useRef(null)
  const hasAnimatedInRef = useRef(false)

  const fetchUsers = async () => {
    try {
      setIsLoading(true)
      const res = await fetch('/api/users')
      if (!res.ok) throw new Error('Failed to fetch users')
      const data = await res.json()
      setUsers(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchUsers()
  }, [])

  useLayoutEffect(() => {
    if (!isLoading && users.length > 0 && listRef.current && !hasAnimatedInRef.current) {
      hasAnimatedInRef.current = true
      const items = listRef.current.querySelectorAll('.users-tab-item-box')
      gsap.fromTo(items, 
        { opacity: 0, y: 15 },
        { opacity: 1, y: 0, duration: 0.3, stagger: 0.04, ease: 'power2.out' }
      )
    }
  }, [isLoading, users])

  const handleApprove = async (id) => {
    try {
      const res = await fetch(`/api/users/${id}/approve?requesterId=1`, { method: 'PUT' })
      if (!res.ok) throw new Error('Failed to approve')
      setUsers(prev => prev.filter(u => u.id !== id))
    } catch (err) {
      alert(err.message)
    }
  }

  const handleMakeAdmin = async (id) => {
    try {
      const res = await fetch(`/api/users/${id}/make-admin?requesterId=1`, { method: 'PUT' })
      if (!res.ok) throw new Error('Failed to make admin')
      
      setUsers(prev => prev.map(u => u.id === id ? { ...u, isAdmin: true } : u))
    } catch (err) {
      alert(err.message)
    }
  }

  const handleDelete = async (id) => {
    try {
      const res = await fetch(`/api/users/${id}?requesterId=1`, { method: 'DELETE' })
      if (!res.ok) throw new Error('Failed to delete user')
      setUsers(prev => prev.filter(u => u.id !== id))
    } catch (err) {
      alert(err.message)
    }
  }

  const toggleExpand = (id) => {
    setExpandedId(prev => prev === id ? null : id)
  }

  const filteredUsers = users.filter(user => 
    user.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.lastName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.email.toLowerCase().includes(searchQuery.toLowerCase())
  )

  return (
    <div className="users-tab-container">
      <div className="users-tab-search-wrapper">
        <input 
          type="text" 
          className="users-tab-search" 
          placeholder="search users" 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>
      
      {isLoading && <div className="users-tab-loading">Loading...</div>}
      {error && <div className="users-tab-error">Error: {error}</div>}
      {!isLoading && !error && filteredUsers.length === 0 && (
        <div className="users-tab-empty">No users found.</div>
      )}
      
      {!isLoading && !error && filteredUsers.length > 0 && (
        <div className="users-tab-list" ref={listRef}>
          {filteredUsers.map(user => (
            <UserItem 
              key={user.id} 
              user={user} 
              isExpanded={expandedId === user.id} 
              onToggle={toggleExpand}
              onMakeAdmin={handleMakeAdmin}
              onApprove={handleApprove}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </div>
  )
}
