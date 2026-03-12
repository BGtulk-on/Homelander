import { useState } from 'react'
import LaptopModel from './LaptopModel'
import './AccessGate.css'

export default function AccessGate() {
  const [wantsExistingAccess, setWantsExistingAccess] = useState(true)
  const [isTyping, setIsTyping] = useState(false)
  const [activeInputType, setActiveInputType] = useState('text')
  const [activeInputValue, setActiveInputValue] = useState('')
  const [activeFieldIndex, setActiveFieldIndex] = useState(0)

  const toggleAccessMode = () => {
    setWantsExistingAccess(!wantsExistingAccess)
    setIsTyping(false)
  }

  const handleSubmission = (e) => {
    e.preventDefault()
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
    setActiveInputValue(e.target.value)
  }

  return (
    <div className="gate-wrapper">
      <div className="gate-blank-section">
        <LaptopModel isTyping={isTyping} activeInputValue={activeInputValue} activeInputType={activeInputType} activeFieldIndex={activeFieldIndex} />
      </div>
      <div className="gate-divider"></div>
      <div className="gate-form-section">
        <div className="gate-form-content" onFocus={handleFocus} onBlur={handleBlur} onChange={handleChange}>
          <h2 className="gate-title">{wantsExistingAccess ? 'LOGIN' : 'REGISTER'}</h2>
          
          <form className="gate-form" onSubmit={handleSubmission}>
            <input type="text" placeholder="USERNAME" maxLength={50} />
            
            {!wantsExistingAccess && (
              <input type="email" placeholder="EMAIL" maxLength={50} />
            )}
            
            <input type="password" placeholder="PASSWORD" maxLength={50} />
            
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
