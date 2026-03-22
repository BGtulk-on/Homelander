import React, { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';
import gsap from 'gsap';
import './ZoomableImage.css';

const ZoomableImage = ({ src, alt, className, onError }) => {
  const [isOpen, setIsOpen] = useState(null); // null or {top, left, width, height, naturalWidth, naturalHeight}
  const imgRef = useRef(null);
  const overlayRef = useRef(null);
  const zoomedImgRef = useRef(null);
  const closeBtnRef = useRef(null);

  const handleOpen = () => {
    if (!src || !imgRef.current) return;
    const rect = imgRef.current.getBoundingClientRect();
    
    // Create a temporary image to get natural dimensions if not already available
    const tempImg = new Image();
    tempImg.src = src;
    tempImg.onload = () => {
      setIsOpen({
        top: rect.top,
        left: rect.left,
        width: rect.width,
        height: rect.height,
        naturalWidth: tempImg.naturalWidth,
        naturalHeight: tempImg.naturalHeight
      });
    };
  };

  useEffect(() => {
    if (isOpen && zoomedImgRef.current) {
      // Calculate target dimensions
      const padding = 40;
      const maxWidth = window.innerWidth * 0.9;
      const maxHeight = window.innerHeight * 0.65;
      
      let targetWidth = Math.min(maxWidth, 1000);
      let targetHeight = (targetWidth * isOpen.naturalHeight) / isOpen.naturalWidth;
      
      if (targetHeight > maxHeight) {
        targetHeight = maxHeight;
        targetWidth = (targetHeight * isOpen.naturalWidth) / isOpen.naturalHeight;
      }

      // Initial position from original image
      gsap.set(zoomedImgRef.current, {
        top: isOpen.top,
        left: isOpen.left,
        width: isOpen.width,
        height: isOpen.height,
        xPercent: 0,
        yPercent: 0,
        position: 'fixed',
        objectFit: 'cover',
        margin: 0,
        zIndex: 10000,
        borderRadius: '0px',
      });

      gsap.set(overlayRef.current, { opacity: 0 });
      gsap.set(closeBtnRef.current, { opacity: 0, y: 30 });

      const tl = gsap.timeline();
      tl.to(overlayRef.current, {
        opacity: 1,
        duration: 0.4,
        ease: 'power2.out',
      })
      .to(zoomedImgRef.current, {
        top: '40%', 
        left: '50%',
        xPercent: -50,
        yPercent: -50,
        width: targetWidth,
        height: targetHeight,
        borderRadius: '0px',
        duration: 0.6,
        ease: 'power3.inOut',
      }, 0)
      .to(closeBtnRef.current, {
        opacity: 1,
        y: 0,
        duration: 0.5,
        ease: 'back.out(1.7)',
      }, '-=0.2');
    }
  }, [isOpen]);

  const handleClose = () => {
    if (!imgRef.current) return;
    const rect = imgRef.current.getBoundingClientRect();
    
    const tl = gsap.timeline({
      onComplete: () => setIsOpen(null)
    });

    tl.to(closeBtnRef.current, {
      opacity: 0,
      y: 30,
      duration: 0.3,
      ease: 'power2.in',
    })
    .to(zoomedImgRef.current, {
      top: rect.top,
      left: rect.left,
      xPercent: 0,
      yPercent: 0,
      width: rect.width,
      height: rect.height,
      borderRadius: '0px',
      duration: 0.6,
      ease: 'power3.inOut',
    }, 0)
    .to(overlayRef.current, {
      opacity: 0,
      duration: 0.4,
      ease: 'power2.in',
    }, '-=0.3');
  };

  return (
    <>
      <img
        ref={imgRef}
        src={src}
        alt={alt}
        className={`${className} zoomable-trigger`}
        onClick={handleOpen}
        onError={onError}
        style={{ cursor: 'zoom-in' }}
      />
      {isOpen && createPortal(
        <div className="zoomable-overlay" ref={overlayRef} onClick={(e) => e.target === overlayRef.current && handleClose()}>
          <div className="zoomable-content">
            <img
              ref={zoomedImgRef}
              src={src}
              alt={alt}
              className="zoomed-image"
            />
            <button
              className="zoomable-close-btn"
              ref={closeBtnRef}
              onClick={handleClose}
            >
              CLOSE
            </button>
          </div>
        </div>,
        document.body
      )}
    </>
  );
};

export default ZoomableImage;
