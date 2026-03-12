import { useRef, useEffect, useState, useCallback } from 'react'
import { Canvas, useFrame } from '@react-three/fiber'
import { Environment, Text, RoundedBox } from '@react-three/drei'
import * as THREE from 'three'

function Laptop({ isTyping, activeInputValue, activeInputType, activeFieldIndex, onReady }) {
  const hingeRef = useRef()
  const readyFired = useRef(false)
  const textGroupRef = useRef()
  const prevFieldRef = useRef(activeFieldIndex)
  const swipeOffset = useRef(0)

  useEffect(() => {
    if (prevFieldRef.current !== activeFieldIndex) {
      const direction = activeFieldIndex > prevFieldRef.current ? 1 : -1
      swipeOffset.current = direction * -1.5
      prevFieldRef.current = activeFieldIndex
    }
  }, [activeFieldIndex])

  useFrame((state, delta) => {
    const targetRotation = isTyping ? -0.2 : 1.55
    if (hingeRef.current) {
      hingeRef.current.rotation.x = THREE.MathUtils.lerp(
        hingeRef.current.rotation.x,
        targetRotation,
        delta * 6
      )
    }

    swipeOffset.current = THREE.MathUtils.lerp(swipeOffset.current, 0, delta * 8)
    if (textGroupRef.current) {
      textGroupRef.current.position.y = swipeOffset.current
    }

    if (!readyFired.current) {
      readyFired.current = true
      onReady?.()
    }
  })

  const shellColor = "#A0430A"
  const innerColor = "#1a1a1a"
  const screenColor = "#050505"
  const textColor = "#DFE8E6"

  const shellMaterial = { color: shellColor, roughness: 0.4, metalness: 0.7 }
  const innerMaterial = { color: innerColor, roughness: 0.8, metalness: 0.2 }

  const displayText = !activeInputValue ? '' : (
    activeInputType === 'password' 
      ? '●'.repeat(activeInputValue.length) 
      : Array.from(activeInputValue).map((c, i) => "abcdefghijklmnopqrstuvwxyz"[(c.charCodeAt(0) + i * 7) % 26]).join('')
  )

  const fontUrl = "https://cdn.fontshare.com/wf/O462VY6O6FTQCS72XVMTQHXAM4NN5CY3/TWF57ITZORMJ3MEWLQQIVO6BMXIB6FUR/MJQFMMOTEGNXDVM7HBBDTQHTVB2M7Y6G.woff"

  return (
    <group position={[0, -0.5, 0]}>
      <mesh position={[0, 0.05, 0]}>
        <RoundedBox args={[3.2, 0.1, 2.2]} radius={0.03} smoothness={4}>
          <meshStandardMaterial {...shellMaterial} />
        </RoundedBox>
      </mesh>

      {/* Recessed Keyboard Area */}
      <mesh position={[0, 0.1, -0.1]}>
        <boxGeometry args={[2.8, 0.01, 1]} />
        <meshStandardMaterial {...innerMaterial} />
      </mesh>

      {/* Trackpad */}
      <mesh position={[0, 0.102, 0.7]}>
        <boxGeometry args={[1.2, 0.005, 0.5]} />
        <meshStandardMaterial color="#2a2a2a" roughness={0.6} metalness={0.1} />
      </mesh>

      {/* Small subtle hinge block */}
      <mesh position={[0, 0.1, -1.05]} rotation={[0, 0, Math.PI / 2]}>
        <cylinderGeometry args={[0.04, 0.04, 2.6, 16]} />
        <meshStandardMaterial {...innerMaterial} />
      </mesh>

      <group ref={hingeRef} position={[0, 0.1, -1.1]} rotation={[1.55, 0, 0]}>
        {/* Screen Back */}
        <mesh position={[0, 1.1, 0]}>
          <RoundedBox args={[3.2, 2.2, 0.08]} radius={0.03} smoothness={4}>
            <meshStandardMaterial {...shellMaterial} />
          </RoundedBox>
        </mesh>

        {/* Inner Bezel*/}
        <mesh position={[0, 1.1, 0.041]}>
          <boxGeometry args={[3.1, 2.1, 0.01]} />
          <meshStandardMaterial {...innerMaterial} />
        </mesh>
        
        {/* Screen Display */}
        <mesh position={[0, 1.1, 0.047]}>
          <boxGeometry args={[2.9, 1.8, 0.01]} />
          <meshStandardMaterial color={screenColor} roughness={0.05} metalness={0.8} />
        </mesh>

        {isTyping && (
          <group ref={textGroupRef}>
            <Text
              position={[-1.35, 1.85, 0.055]}
              anchorX="left"
              anchorY="top"
              fontSize={0.2}
              color={textColor}
              font={fontUrl}
            >
              {'>'}
            </Text>
            <Text
              position={[-1.15, 1.85, 0.055]}
              anchorX="left"
              anchorY="top"
              fontSize={0.2}
              color={textColor}
              maxWidth={2.5}
              lineHeight={1.2}
              overflowWrap="break-word"
              letterSpacing={activeInputType === 'password' ? 0.3 : 0.05}
              font={fontUrl}
            >
              {displayText + '｜'}
            </Text>
          </group>
        )}

        {/* Company */}
        <Text
          position={[0, 1.1, -0.042]}
          rotation={[0, Math.PI, 0]}
          fontSize={0.25}
          color={innerColor}
          letterSpacing={0.15}
          font={fontUrl}
        >
          Homelander
        </Text>
      </group>
    </group>
  )
}

export default function LaptopModel({ isTyping, activeInputValue, activeInputType, activeFieldIndex }) {
  const wrapperRef = useRef(null)
  const [modelReady, setModelReady] = useState(false)

  const handleReady = useCallback(() => setModelReady(true), [])

  useEffect(() => {
    if (!modelReady || !wrapperRef.current) return
    import('gsap').then(({ gsap }) => {
      gsap.fromTo(wrapperRef.current,
        { x: '-100%', opacity: 0 },
        { x: '0%', opacity: 1, duration: 1.2, ease: 'power3.out' }
      )
    })
  }, [modelReady])

  return (
    <div
      ref={wrapperRef}
      style={{ position: 'relative', width: '500px', height: '500px', zIndex: 1, pointerEvents: 'none', opacity: 0 }}
    >
      <Canvas camera={{ position: [0, 2.0, 6.5], fov: 45 }}>
        <ambientLight intensity={0.6} />
        <directionalLight position={[5, 10, 5]} intensity={1.5} />
        <Environment preset="city" />
        
        <group rotation={[0.2, 0.8, 0]}>
          <Laptop isTyping={isTyping} activeInputValue={activeInputValue} activeInputType={activeInputType} activeFieldIndex={activeFieldIndex} onReady={handleReady} />
        </group>
      </Canvas>
    </div>
  )
}
