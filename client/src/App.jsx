import { useState, useRef, useEffect } from 'react'
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
        const res = await fetch('/api/auth/me')
        if (res.ok) {
          const userData = await res.json()
          setUser(userData)
          
          const isAdmin = userData.role === 'ADMIN' || userData.role === 'SUPERUSER'
          if (isAdmin) {
            gsap.set(dividerRef.current, {
              height: '100%',
              left: '20%',
              opacity: 1
            })
          } else {
            gsap.set(dividerRef.current, {
              opacity: 0
            })
          }
          
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

  if (isInitialLoading) {
    return <div style={{ 
      background: '#DFE8E6', 
      height: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center',
      color: '#A0430A',
      fontFamily: 'Clash Grotesk, sans-serif'
    }}>LOADING...</div>
  }

  const handleLogin = (userData) => {
    // New role-based check from the last backend commits
    const isAdmin = userData.role === 'ADMIN' || userData.role === 'SUPERUSER'
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
    gsap.set(dividerRef.current, { height: '60vh', left: '50%', opacity: 1 })
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
