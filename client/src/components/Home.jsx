import { useEffect, useRef, useState } from 'react'
import gsap from 'gsap'
import './Home.css'

export default function Home({ userRole, onLogout }) {
  const contentRef = useRef(null)
  const navRef = useRef(null)
  const [isUsersAnimating, setIsUsersAnimating] = useState(false)
  const [isEquipmentAnimating, setIsEquipmentAnimating] = useState(false)
  const [isRequestsAnimating, setIsRequestsAnimating] = useState(false)

  useEffect(() => {
    gsap.fromTo(contentRef.current,
      { opacity: 0, x: 20 },
      { opacity: 1, x: 0, duration: 1, delay: 0.2, ease: 'power3.out' }
    )

    if (navRef.current) {
      const icons = navRef.current.querySelectorAll('.home-nav-icon-btn')
      const logoutBtn = navRef.current.querySelector('.home-nav-logout')
      
      const tl = gsap.timeline({ delay: 0.3 })

      tl.fromTo([...icons, logoutBtn],
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

  const triggerUsersAnimation = () => {
    if (isUsersAnimating) return
    setIsUsersAnimating(true)
    setTimeout(() => setIsUsersAnimating(false), 700)
  }

  const triggerEquipmentAnimation = () => {
    if (isEquipmentAnimating) return
    setIsEquipmentAnimating(true)
    setTimeout(() => setIsEquipmentAnimating(false), 800)
  }

  const triggerRequestsAnimation = () => {
    if (isRequestsAnimating) return
    setIsRequestsAnimating(true)
    setTimeout(() => setIsRequestsAnimating(false), 800)
  }

  return (
    <div className="home-wrapper">
      {userRole === 'admin' && (
        <aside className="home-sidebar">
          <div className="home-sidebar-content">
            {/* Top content */}
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
            <button className="home-nav-logout" onClick={onLogout}>LOGOUT</button>
          </nav>
        </aside>
      )}
      <div className="home-dashboard-area" ref={contentRef}>
      </div>
    </div>
  )
}
