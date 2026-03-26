import { useState, useRef, useEffect } from 'react'
import LaptopModel from './LaptopModel'
import gsap from 'gsap'
import './AccessGate.css'
import { sanitizeInput } from '../utils/sanitizer'

export default function AccessGate({ onLogin, isExiting }) {
  const [wantsExistingAccess, setWantsExistingAccess] = useState(true)
  const [isTyping, setIsTyping] = useState(false)
  const [activeInputType, setActiveInputType] = useState('text')
  const [activeInputValue, setActiveInputValue] = useState('')
  const [activeFieldIndex, setActiveFieldIndex] = useState(0)
  
  const leftSectionRef = useRef(null)
  const rightSectionRef = useRef(null)

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: ''
  })

  useEffect(() => {
    if (isExiting) {
      gsap.to(leftSectionRef.current, {
        x: '-100%',
        opacity: 0,
        duration: 0.8,
        ease: 'power3.in'
      })
      gsap.to(rightSectionRef.current, {
        x: '100%',
        opacity: 0,
        duration: 0.8,
        ease: 'power3.in'
      })
    }
  }, [isExiting])

  const toggleAccessMode = () => {
    setWantsExistingAccess(!wantsExistingAccess)
    setIsTyping(false)
  }

  const handleSubmission = async (e) => {
    e.preventDefault()
    
    try {
      if (wantsExistingAccess) {
        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            email: formData.email,
            password: formData.password
          })
        })

        if (!response.ok) {
          let errorMessage = 'Invalid credentials'
          try {
            const errorJson = await response.json()
            errorMessage = errorJson.message || errorMessage
          } catch (e) {
          }
          throw new Error(errorMessage)
        }

        const data = await response.json()
        onLogin(data)
      } else {
        const response = await fetch('/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            first_name: formData.firstName,
            last_name: formData.lastName,
            email: formData.email,
            password: formData.password
          })
        })

        if (!response.ok) {
          let errorMessage = 'Registration failed'
          try {
            const errorJson = await response.json()
            errorMessage = errorJson.message || errorMessage
          } catch (e) {
          }
          throw new Error(errorMessage)
        }

        alert('Registration successful! You can now login.')
        setWantsExistingAccess(true)
        setIsTyping(false)
      }
    } catch (err) {
      alert(err.message)
    }
  }

  const handleFocus = (e) => {
    if (e.target.tagName === 'INPUT') {
      const inputs = Array.from(e.currentTarget.querySelectorAll('input'))
      const idx = inputs.indexOf(e.target)
      setIsTyping(true)
      setActiveInputType(e.target.type)
      setActiveInputValue(e.target.value)
      setActiveFieldIndex(idx)
    }
  }
  
  const handleBlur = (e) => {
    if (!e.currentTarget.contains(e.relatedTarget)) {
      const inputs = e.currentTarget.querySelectorAll('input')
      const hasContent = Array.from(inputs).some(inp => inp.value.length > 0)
      if (!hasContent) setIsTyping(false)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    const sanitizedValue = sanitizeInput(value)
    setFormData(prev => ({ ...prev, [name]: sanitizedValue }))
    setActiveInputValue(sanitizedValue)
  }

  return (
    <div className="gate-wrapper">
      <div className="gate-blank-section" ref={leftSectionRef}>
        <LaptopModel 
          isTyping={isTyping} 
          activeInputValue={activeInputValue} 
          activeInputType={activeInputType} 
          activeFieldIndex={activeFieldIndex} 
        />
      </div>
      
      <div className="gate-form-section" ref={rightSectionRef}>
        <div className="gate-form-content" onFocus={handleFocus} onBlur={handleBlur} onChange={handleChange}>
          <h2 className="gate-title">{wantsExistingAccess ? 'LOGIN' : 'REGISTER'}</h2>
          
          <form className="gate-form" onSubmit={handleSubmission} autoComplete="off">
            {!wantsExistingAccess && (
              <>
                <input 
                  type="text" 
                  name="firstName"
                  placeholder="FIRST NAME" 
                  maxLength={50} 
                  value={formData.firstName}
                  onChange={handleChange}
                />
                <input 
                  type="text" 
                  name="lastName"
                  placeholder="LAST NAME" 
                  maxLength={50} 
                  value={formData.lastName}
                  onChange={handleChange}
                />
              </>
            )}

            <input 
              type="email" 
              name="email"
              placeholder="EMAIL" 
              maxLength={50} 
              value={formData.email}
              onChange={handleChange}
            />
            
            <input 
              type="password" 
              name="password"
              placeholder="PASSWORD" 
              maxLength={50} 
              value={formData.password}
              onChange={handleChange}
            />
            
            <button type="submit" className="gate-submit">
              {wantsExistingAccess ? 'ENTER' : 'CREATE'}
            </button>
          </form>
          
          <button 
            type="button" 
            className="gate-mode-switch" 
            onClick={toggleAccessMode}
          >
            {wantsExistingAccess ? 'Don\'t have an account?' : 'Already have an account?'}
          </button>
        </div>
      </div>
    </div>
  )
}
