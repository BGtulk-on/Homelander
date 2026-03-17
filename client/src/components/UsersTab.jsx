import { useEffect, useState, useRef, useLayoutEffect } from 'react'
import gsap from 'gsap'
import './UsersTab.css'

function ActionConfirmBtn({ actionText, confirmText, onClick }) {
  const [isConfirming, setIsConfirming] = useState(false)
  const textContainerRef = useRef(null)
  const timeoutRef = useRef(null)

  useEffect(() => {
    return () => clearTimeout(timeoutRef.current)
  }, [])

  const handleClick = (e) => {
    e.stopPropagation()
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
    <button className="users-tab-action-btn" onClick={handleClick}>
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
        <span className="users-tab-username">
          {user.firstName} {user.lastName}
        </span>
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
        <div className="users-tab-body-header">
          <p className="users-tab-email">{user.email}</p>
          {user.isAdmin && (
            <span 
              className="users-tab-badge-admin" 
              ref={badgeRef}
              style={{ display: 'inline-flex', opacity: 0 }}
            >
              Admin
            </span>
          )}
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

  useEffect(() => {
    if (!isLoading && users.length > 0 && listRef.current && !hasAnimatedInRef.current) {
      hasAnimatedInRef.current = true
      const items = listRef.current.querySelectorAll('.users-tab-item-box')
      gsap.fromTo(items, 
        { opacity: 0, x: -10 },
        { opacity: 1, x: 0, duration: 0.3, stagger: 0.04, ease: 'power2.out' }
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
          placeholder="search" 
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
