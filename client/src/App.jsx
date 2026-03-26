import { useState, useRef, useEffect, useLayoutEffect } from 'react'
import gsap from 'gsap'
import AccessGate from './components/AccessGate'
import Home from './components/Home'
import './index.css'

const API_BASE_URL = 'homelander-uktc-dydzf7dafpg4cwb3.swedencentral-01.azurewebsites.net';

function App() {
  const [user, setUser] = useState(null)
  const [isTransitioning, setIsTransitioning] = useState(false)
  const dividerRef = useRef(null)
  const [appPhase, setAppPhase] = useState('GATE')
  const [isInitialLoading, setIsInitialLoading] = useState(true)

  useEffect(() => {
    const checkSession = async () => {
      try {
        const minWait = new Promise(resolve => setTimeout(resolve, 400));
        const fetchMe = fetch('/api/auth/me');

        const [res] = await Promise.all([fetchMe, minWait]);

        if (res.ok) {
          const userData = await res.json()
          setUser(userData)
          setAppPhase('HOME')
        }
      } catch (err) {
        console.error('Session check failed:', err)
      } finally {
        setIsInitialLoading(false)
      }
    }
    checkSession()
  }, [])

  useLayoutEffect(() => {
    if (appPhase === 'HOME' && user && dividerRef.current) {
      const isAdmin = user.role === 'ADMIN' || user.role === 'SUPERUSER'
      const isMobile = window.innerWidth <= 768
      
      if (isAdmin) {
        if (isMobile) {
          gsap.set(dividerRef.current, {
            width: '100%',
            height: '2px',
            top: '0%',
            left: '0%',
            transform: 'none',
            position: 'absolute',
            opacity: 1
          })
        } else {
          gsap.set(dividerRef.current, {
            width: '2px',
            height: '100%',
            left: '20%',
            top: '50%',
            transform: 'translate(-50%, -50%)',
            position: 'absolute',
            opacity: 1
          })
        }
      } else {
        gsap.set(dividerRef.current, {
          opacity: 0
        })
      }
    }
  }, [appPhase, user])

  if (isInitialLoading) {
    return (
      <main className="app-container phase-GATE" style={{ opacity: 1 }}>
        <div className="shared-divider" style={{ height: '60vh', left: '50%', opacity: 0.1 }}></div>
        <div style={{ 
          height: '100vh', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          color: '#A0430A',
          fontFamily: 'Clash Grotesk, sans-serif',
          letterSpacing: '4px',
          fontSize: '0.8rem',
          opacity: 0.6
        }}>INITIALIZING...</div>
      </main>
    )
  }

  const handleLogin = (userData) => {
    // New role-based check from the last backend commits
    const isAdmin = userData.role === 'ADMIN' || userData.role === 'SUPERUSER'
    const isMobile = window.innerWidth <= 768

    if (isAdmin) {
      setIsTransitioning(true)
      setAppPhase('TRANSITION')
      
      const tl = gsap.timeline({
        onComplete: () => {
          setUser(userData)
          setAppPhase('HOME')
          setIsTransitioning(false)
        }
      })

      if (isMobile) {
        tl.to(dividerRef.current, {
          width: '100%',
          duration: 0.8,
          ease: 'power3.inOut'
        })
        .to(dividerRef.current, {
          top: '0%',
          left: '0%',
          transform: 'none',
          position: 'absolute',
          duration: 1,
          ease: 'power4.inOut'
        }, '-=0.2')
      } else {
        tl.to(dividerRef.current, {
          height: '100%',
          duration: 0.8,
          ease: 'power3.inOut'
        })
        .to(dividerRef.current, {
          left: '20%',
          duration: 1,
          ease: 'power4.inOut'
        }, '-=0.2')
      }
    } else {
      setIsTransitioning(true)
      setAppPhase('TRANSITION')
      
      gsap.to(dividerRef.current, {
        opacity: 0,
        duration: 0.8,
        ease: 'power3.in',
        onComplete: () => {
          setUser(userData)
          setAppPhase('HOME')
          setIsTransitioning(false)
        }
      })
    }
  }

  const handleLogout = async () => {
    try {
      await fetch('/api/auth/logout', { method: 'POST' })
    } catch (err) {
      console.error('Logout error:', err)
    }
    setUser(null)
    setAppPhase('GATE')
    
    const isMobile = window.innerWidth <= 768
    if (isMobile) {
      gsap.set(dividerRef.current, { 
        width: '80vw', 
        height: '2px', 
        top: '0', 
        left: '0', 
        margin: '0 auto',
        position: 'relative',
        transform: 'none',
        opacity: 1,
        clearProps: 'all' 
      })
      gsap.set(dividerRef.current, { 
        width: '80vw', 
        height: '2px', 
        top: '0', 
        position: 'relative',
        margin: '0 auto',
        left: '0',
        transform: 'none',
        opacity: 1 
      })
    } else {
      gsap.set(dividerRef.current, { 
        width: '2px',
        height: '60vh', 
        left: '50%', 
        top: '50%',
        opacity: 1,
        clearProps: 'all'
      })
      gsap.set(dividerRef.current, { 
        width: '2px',
        height: '60vh', 
        left: '50%', 
        top: '50%',
        opacity: 1 
      })
    }
  }

  return (
    <main className={`app-container phase-${appPhase}`}>
      <div className="shared-divider" ref={dividerRef}></div>
      
      {appPhase !== 'HOME' && (
        <AccessGate 
          onLogin={handleLogin} 
          isExiting={appPhase === 'TRANSITION'} 
        />
      )}
      
      {appPhase === 'HOME' && (
        <Home user={user} onLogout={handleLogout} />
      )}
    </main>
  )
}

export default App
