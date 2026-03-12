import { useState, useRef, useEffect } from 'react'
import gsap from 'gsap'
import AccessGate from './components/AccessGate'
import Home from './components/Home'
import './index.css'

function App() {
  const [user, setUser] = useState(null)
  const [isTransitioning, setIsTransitioning] = useState(false)
  const dividerRef = useRef(null)
  const [appPhase, setAppPhase] = useState('GATE')

  const handleLogin = (role) => {
    if (role === 'admin') {
      setIsTransitioning(true)
      setAppPhase('TRANSITION')
      
      const tl = gsap.timeline({
        onComplete: () => {
          setUser(role)
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
          setUser(role)
          setAppPhase('HOME')
          setIsTransitioning(false)
        }
      })
    }
  }

  const handleLogout = () => {
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
        <Home userRole={user} onLogout={handleLogout} />
      )}
    </main>
  )
}

export default App
