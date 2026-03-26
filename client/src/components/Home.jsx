import { useEffect, useRef, useState, useLayoutEffect } from 'react'
import gsap from 'gsap'
import UsersTab from './UsersTab'
import EquipmentTab from './EquipmentTab'
import RequestsTab from './RequestsTab'
import RoomsTab from './RoomsTab'
import './Home.css'

export default function Home({ user, onLogout }) {
  const contentRef = useRef(null)
  const navRef = useRef(null)
  const [isUsersAnimating, setIsUsersAnimating] = useState(false)
  const [isEquipmentAnimating, setIsEquipmentAnimating] = useState(false)
  const [isRequestsAnimating, setIsRequestsAnimating] = useState(false)
  const [activeTab, setActiveTab] = useState('equipment')
  const [refreshTrigger, setRefreshTrigger] = useState(0)
  const [showProfile, setShowProfile] = useState(false)
  const [isScrolled, setIsScrolled] = useState(false)
  
  const titleRef = useRef(null)
  const profileRef = useRef(null)
  const dropdownRef = useRef(null)
  const letterRef = useRef(null)
  const menuRef = useRef(null)
  const timelineRef = useRef(null)

  const handleEquipmentChange = () => {
    setRefreshTrigger(prev => prev + 1)
  }

  const renderSidebarContent = () => {
    switch (activeTab) {
      case 'users':
        return <UsersTab currentUser={user} />
      case 'requests':
        return <RequestsTab currentUser={user} />
      case 'equipment':
      default:
        return <EquipmentTab onEquipmentChange={handleEquipmentChange} currentUser={user} />
    }
  }

  useLayoutEffect(() => {
    gsap.fromTo(contentRef.current,
      { opacity: 0, x: 20 },
      { opacity: 1, x: 0, duration: 1, delay: 0.2, ease: 'power3.out' }
    )

    if (navRef.current) {
      const icons = navRef.current.querySelectorAll('.home-nav-icon-btn')
      
      const tl = gsap.timeline({ delay: 0.3 })

      tl.fromTo(icons,
        { autoAlpha: 0, y: 40 },
        { 
          autoAlpha: 1, 
          y: 0, 
          duration: 0.2, 
          stagger: 0.06, 
          ease: 'power3.out',
          onComplete: () => {
            gsap.to(icons, { opacity: 0.6, duration: 0.3 })
          }
        }
      )
    }
  }, [])

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setShowProfile(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useEffect(() => {
    const area = contentRef.current
    if (!area) return

    const handleScroll = () => {
      const scrollY = area.scrollTop
      const progress = Math.min(scrollY / 150, 1) 
      setIsScrolled(scrollY > 20)

      if (titleRef.current) {
        gsap.to(titleRef.current, {
          fontSize: (8 - 6 * progress) + 'rem',
          marginBottom: (3 - 3 * progress) + 'rem',
          duration: 0.1,
          overwrite: 'auto',
          ease: 'power1.out'
        })
      }

      if (profileRef.current) {
        gsap.to(profileRef.current, {
          scale: 1 - 0.15 * progress,
          duration: 0.1,
          overwrite: 'auto',
          ease: 'power1.out'
        })
      }

      const header = area.querySelector('.dashboard-sticky-header')
      if (header) {
        gsap.to(header, {
          paddingTop: (4 - 2 * progress) + 'rem',
          paddingBottom: (1 - 0.2 * progress) + 'rem',
          duration: 0.1,
          overwrite: 'auto',
          ease: 'power1.out'
        })
      }
    }

    area.addEventListener('scroll', handleScroll)
    return () => area.removeEventListener('scroll', handleScroll)
  }, [])

  useLayoutEffect(() => {
    if (!profileRef.current || !letterRef.current || !menuRef.current) return

    if (!timelineRef.current) {
      const tl = gsap.timeline({ paused: true, reversed: true })
      
      tl.to(letterRef.current, { 
        opacity: 0, 
        scale: 0.8, 
        duration: 0.2, 
        ease: 'power2.in' 
      })
      .to(profileRef.current, {
        width: 320,
        height: 180,
        borderRadius: '20px',
        duration: 0.45,
        ease: 'power4.inOut'
      }, '-=0.1')
      .fromTo(menuRef.current, 
        { autoAlpha: 0, y: 10 }, 
        { autoAlpha: 1, y: 0, duration: 0.25, ease: 'power2.out' },
        '-=0.15'
      )
      
      timelineRef.current = tl
    }

    if (showProfile) {
      timelineRef.current.play()
    } else {
      timelineRef.current.reverse()
    }
  }, [showProfile])

  const triggerUsersAnimation = () => {
    if (isUsersAnimating) return
    setIsUsersAnimating(true)
    setActiveTab('users')
    setTimeout(() => setIsUsersAnimating(false), 700)
  }

  const triggerEquipmentAnimation = () => {
    if (isEquipmentAnimating) return
    setIsEquipmentAnimating(true)
    setActiveTab('equipment')
    setTimeout(() => setIsEquipmentAnimating(false), 800)
  }

  const triggerRequestsAnimation = () => {
    if (isRequestsAnimating) return
    setIsRequestsAnimating(true)
    setActiveTab('requests')
    setTimeout(() => setIsRequestsAnimating(false), 800)
  }

  return (
    <div className="home-wrapper">
      {(user?.role === 'ADMIN' || user?.role === 'SUPERUSER') && (
        <aside className="home-sidebar">
          <div className="home-sidebar-content">
            {renderSidebarContent()}
          </div>
          
          <nav className="home-nav-bottom" ref={navRef}>
            <div className="home-nav-icons">
              <button 
                className="home-nav-icon-btn" 
                title="USERS"
                onClick={triggerUsersAnimation}
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path 
                    className={isUsersAnimating ? 'animate-head' : ''}
                    fillRule="evenodd" 
                    clipRule="evenodd" 
                    d="M16 7C16 9.20914 14.2091 11 12 11C9.79086 11 8 9.20914 8 7C8 4.79086 9.79086 3 12 3C14.2091 3 16 4.79086 16 7ZM14 7C14 8.10457 13.1046 9 12 9C10.8954 9 10 8.10457 10 7C10 5.89543 10.8954 5 12 5C13.1046 5 14 5.89543 14 7Z" 
                    fill="currentColor"
                  />
                  <path 
                    className={isUsersAnimating ? 'animate-body' : ''}
                    d="M16 15C16 14.4477 15.5523 14 15 14H9C8.44772 14 8 14.4477 8 15V21H6V15C6 13.3431 7.34315 12 9 12H15C16.6569 12 18 13.3431 18 15V21H16V15Z" 
                    fill="currentColor"
                  />
                </svg>
              </button>
              <button 
                className="home-nav-icon-btn" 
                title="EQUIPMENT"
                onClick={triggerEquipmentAnimation}
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path 
                    className={isEquipmentAnimating ? 'animate-screen' : ''}
                    fillRule="evenodd" 
                    clipRule="evenodd" 
                    d="M3 6C3 4.89543 3.89543 4 5 4H19C20.1046 4 21 4.89543 21 6V14C21 15.1046 20.1046 16 19 16H5C3.89543 16 3 15.1046 3 14V6ZM5 6H19V14H5V6Z" 
                    fill="currentColor"
                  />
                  <path 
                    className={isEquipmentAnimating ? 'animate-base' : ''}
                    d="M2 18C1.44772 18 1 18.4477 1 19C1 19.5523 1.44772 20 2 20H22C22.5523 20 23 19.5523 23 19C23 18.4477 22.5523 18 22 18H2Z" 
                    fill="currentColor"
                  />
                </svg>
              </button>
              <button 
                className="home-nav-icon-btn" 
                title="REQUESTS"
                onClick={triggerRequestsAnimation}
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <g className={isRequestsAnimating ? "animate-envelope" : ""}>
                    <rect 
                      x="4" 
                      y="5.838" 
                      width="16" 
                      height="12.324" 
                      rx="1" 
                      stroke="currentColor" 
                      strokeWidth="2" 
                    />
                    <path 
                      className={isRequestsAnimating ? "animate-flap" : ""}
                      d="M4 5.838 L12 12.815 L20 5.838" 
                      stroke="currentColor" 
                      strokeWidth="2" 
                      strokeLinejoin="round" 
                      strokeLinecap="round" 
                    />
                  </g>
                </svg>
              </button>
            </div>
          </nav>
        </aside>
      )}
      <div className="home-dashboard-area" ref={contentRef}>
        <header className="dashboard-sticky-header">
          <h1 className="rooms-title" ref={titleRef}>Rooms</h1>
          
          <div 
            className="home-profile-card" 
            ref={profileRef}
            onClick={(e) => {
              if (!showProfile) setShowProfile(true)
            }}
          >
            <div className="profile-letter-box" ref={letterRef}>
              {user?.email?.charAt(0).toUpperCase() || '?'}
            </div>
            
            <div className="profile-menu-content" ref={menuRef} style={{ visibility: 'hidden' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div className="profile-dropdown-info">
                  <span className="profile-name">{user?.email}</span>
                  <span className="profile-role">{user?.role}</span>
                </div>
                <button 
                  className="profile-logout-btn" 
                  onClick={onLogout}
                  style={{ padding: '0.4rem', borderRadius: '50%', background: 'rgba(223,232,230,0.1)' }}
                  title="Logout"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                </button>
              </div>
              
              <div className="profile-dropdown-divider" />
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <span className="profile-role" style={{ fontSize: '0.6rem', marginBottom: '2px' }}>MY LOAN HISTORY</span>
                <div style={{ display: 'flex', gap: '5px' }}>
                  <button 
                    className="profile-logout-btn" 
                    onClick={(e) => { e.stopPropagation(); window.location.href=`/reports/my/export?userId=${user.id}&format=csv` }}
                    style={{ fontSize: '0.7rem', padding: '0.4rem', flex: 1, justifyContent: 'center' }}
                  >
                    CSV
                  </button>
                  <button 
                    className="profile-logout-btn" 
                    onClick={(e) => { e.stopPropagation(); window.location.href=`/reports/my/export?userId=${user.id}&format=pdf` }}
                    style={{ fontSize: '0.7rem', padding: '0.4rem', flex: 1, justifyContent: 'center' }}
                  >
                    PDF
                  </button>
                </div>
              </div>
            </div>
          </div>
        </header>
        
        <RoomsTab user={user} refreshTrigger={refreshTrigger} />
      </div>
    </div>
  )
}
