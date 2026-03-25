import { useState, useRef, useEffect } from 'react'

export default function LazyItem({ children, estimatedHeight = '60px', rootMargin = '300px' }) {
  const [isVisible, setIsVisible] = useState(false)
  const [hasRendered, setHasRendered] = useState(false)
  const containerRef = useRef(null)

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true)
          setHasRendered(true)
        } else {
          setIsVisible(false)
        }
      },
      { rootMargin }
    )

    if (containerRef.current) {
      observer.observe(containerRef.current)
    }

    return () => {
      if (containerRef.current) {
        observer.unobserve(containerRef.current)
      }
    }
  }, [rootMargin])

  return (
    <div
      ref={containerRef}
      style={{
        minHeight: isVisible || hasRendered ? 'auto' : estimatedHeight,
        width: '100%'
      }}
    >
      {isVisible || hasRendered ? children : null}
    </div>
  )
}
