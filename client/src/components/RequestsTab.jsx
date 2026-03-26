import { useState, useEffect, useRef, useLayoutEffect } from 'react'
import gsap from 'gsap'
import LazyItem from './LazyItem'
import './RequestsTab.css'

function RequestItem({ request, index, isExpanded, onToggle, onApprove, onReject }) {
  const bodyRef = useRef(null)
  const containerRef = useRef(null)

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A'
    return new Date(dateString).toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  useLayoutEffect(() => {
    if (!bodyRef.current) return
    
    if (!containerRef.current.dataset.initialized) {
      containerRef.current.dataset.initialized = 'true'
      gsap.set(bodyRef.current, {
        height: isExpanded ? 'auto' : 0,
        opacity: isExpanded ? 1 : 0,
        paddingTop: isExpanded ? '4px' : 0,
        paddingBottom: isExpanded ? '8px' : 0,
        borderTopColor: isExpanded ? 'rgba(160, 67, 10, 0.4)' : 'transparent'
      })
      return
    }

    if (isExpanded) {
      gsap.to(bodyRef.current, {
        height: 'auto',
        opacity: 1,
        paddingTop: '4px',
        paddingBottom: '8px',
        borderTopColor: 'rgba(160, 67, 10, 0.4)',
        duration: 0.3,
        ease: 'power2.inOut'
      })
    } else {
      gsap.to(bodyRef.current, {
        height: 0,
        opacity: 0,
        paddingTop: 0,
        paddingBottom: 0,
        borderTopColor: 'transparent',
        duration: 0.3,
        ease: 'power2.inOut'
      })
    }
  }, [isExpanded])

  useLayoutEffect(() => {
    if (containerRef.current && !containerRef.current.dataset.entered) {
      containerRef.current.dataset.entered = 'true'
      gsap.fromTo(containerRef.current, 
        { opacity: 0, y: 15 },
        { 
          opacity: 1, 
          y: 0, 
          duration: 0.45, 
          delay: Math.min(index * 0.05, 0.8),
          ease: 'power2.out' 
        }
      )
    }
  }, [index])

  return (
    <div className={`requests-tab-item-box ${isExpanded ? 'expanded' : ''}`} ref={containerRef}>
      <button className="requests-tab-item-header" onClick={() => onToggle(request.id)}>
        <div className="requests-tab-header-main">
          <span className="requests-tab-username">{request.user.firstName} {request.user.lastName}</span>
          <span className={`requests-tab-status-tag status-${request.requestStatus.toLowerCase()}`}>
            {request.requestStatus}
          </span>
        </div>
        <svg 
          className={`requests-tab-icon ${isExpanded ? 'expanded' : ''}`} 
          viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
        >
          <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </button>

      <div className="requests-tab-item-body" ref={bodyRef} style={{ overflow: 'hidden' }}>
        <div className="requests-tab-body-info">
          <div className="requests-info-grid">
            <div className="requests-info-item full-width" title="Equipment">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
              </span>
              <span className="info-value">{request.equipment.name}</span>
            </div>
            <div className="requests-info-item" title="Serial Number">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M4 9h16"/><path d="M4 15h16"/><path d="M10 3L8 21"/><path d="M16 3l-2 18"/></svg>
              </span>
              <span className="info-value">{request.equipment.serialNumber}</span>
            </div>
            <div className="requests-info-item" title="Email">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
              </span>
              <span className="info-value">{request.user.email}</span>
            </div>
            <div className="requests-info-item full-width" title="Period">
              <span className="info-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              </span>
              <span className="info-value">{formatDate(request.requestedStartDate)} - {formatDate(request.requestedEndDate)}</span>
            </div>
          </div>

          <div className="requests-tab-actions">
            {request.requestStatus === 'PENDING' ? (
              <div className="request-actions-row">
                <button className="requests-tab-action-btn reject" onClick={() => onReject(request.id)}>
                  Reject
                </button>
                <button className="requests-tab-action-btn approve" onClick={() => onApprove(request.id)}>
                  Approve
                </button>
              </div>
            ) : (
              <div className="request-status-info">
                This request is <strong>{request.requestStatus}</strong>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default function RequestsTab({ currentUser }) {
  const [requests, setRequests] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [expandedId, setExpandedId] = useState(null)
  const [showRejected, setShowRejected] = useState(false)
  const [showReturned, setShowReturned] = useState(false)
  const listRef = useRef(null)
  const rejectedRef = useRef(null)
  const returnedRef = useRef(null)

  const fetchRequests = async () => {
    try {
      setIsLoading(true)
      const res = await fetch('/api/requests/all')
      if (!res.ok) throw new Error('Failed to fetch requests')
      const data = await res.json()
      setRequests(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err.message)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchRequests()
  }, [])

  useLayoutEffect(() => {
    // Entrance handled by items
  }, [isLoading, requests.length])

  useLayoutEffect(() => {
    if (rejectedRef.current) {
      if (showRejected) {
        gsap.to(rejectedRef.current, { height: 'auto', opacity: 1, marginTop: '0.8rem', duration: 0.4, ease: 'power2.out' })
      } else {
        gsap.to(rejectedRef.current, { height: 0, opacity: 0, marginTop: 0, duration: 0.3, ease: 'power2.in' })
      }
    }
  }, [showRejected])

  useLayoutEffect(() => {
    if (returnedRef.current) {
      if (showReturned) {
        gsap.to(returnedRef.current, { height: 'auto', opacity: 1, marginTop: '0.8rem', duration: 0.4, ease: 'power2.out' })
      } else {
        gsap.to(returnedRef.current, { height: 0, opacity: 0, marginTop: 0, duration: 0.3, ease: 'power2.in' })
      }
    }
  }, [showReturned])

  const handleApprove = async (id) => {
    try {
      const request = requests.find(r => r.id === id)
      if (!request) return

      const res = await fetch(`/api/requests/${id}/approve?adminId=${currentUser.id}`, {
        method: 'PUT'
      })
      if (!res.ok) throw new Error('Failed to approve')

      await fetch(`/api/equipment/${request.equipment.id}/assign?personName=${request.user.firstName} ${request.user.lastName}`, {
        method: 'PATCH'
      })

      fetchRequests()
    } catch (err) { alert(err.message) }
  }

  const handleReject = async (id) => {
    try {
      const res = await fetch(`/api/requests/${id}/reject?adminId=${currentUser.id}`, {
        method: 'PUT'
      })
      if (!res.ok) throw new Error('Failed to reject')
      fetchRequests()
    } catch (err) { alert(err.message) }
  }

  const activeRequests = requests.filter(r => r.requestStatus !== 'REJECTED' && r.requestStatus !== 'APPROVED' && r.requestStatus !== 'RETURNED')
  const approvedRequests = requests.filter(r => r.requestStatus === 'APPROVED')
  const rejectedRequests = requests.filter(r => r.requestStatus === 'REJECTED')
  const returnedRequests = requests.filter(r => r.requestStatus === 'RETURNED')

  if (isLoading) return <div className="requests-tab-loading">Loading...</div>

  return (
    <div className="requests-tab-container">
      <div className="requests-tab-title-wrapper">
        <h2 className="requests-tab-title">Requests</h2>
      </div>

      <div style={{ padding: '0 0.5rem', marginBottom: '1.2rem', display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
        <span style={{ fontSize: '0.65rem', fontWeight: 'bold', color: '#A0430A', opacity: 0.8 }}>ALL REQUESTS REPORT</span>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button 
            className="users-tab-action-btn export" 
            style={{ fontSize: '0.7rem', padding: '0.3rem 0.6rem', borderStyle: 'dashed', flex: 1, justifyContent: 'center' }}
            onClick={() => window.location.href=`/reports/requests/all/export?requestingUserId=${currentUser.id}&format=csv`}
          >
            CSV
          </button>
          <button 
            className="users-tab-action-btn export" 
            style={{ fontSize: '0.7rem', padding: '0.3rem 0.6rem', borderStyle: 'dashed', flex: 1, justifyContent: 'center' }}
            onClick={() => window.location.href=`/reports/requests/all/export?requestingUserId=${currentUser.id}&format=pdf`}
          >
            PDF
          </button>
        </div>
      </div>
      
      {error && <div className="requests-tab-error">Error: {error}</div>}
      
      <div className="requests-tab-list" ref={listRef}>
        {activeRequests.length > 0 ? activeRequests.map((req, index) => (
          <LazyItem key={req.id} estimatedHeight="54px">
            <RequestItem 
              request={req} 
              index={index}
              isExpanded={expandedId === req.id}
              onToggle={id => setExpandedId(expandedId === id ? null : id)}
              onApprove={handleApprove} 
              onReject={handleReject}
            />
          </LazyItem>
        )) : activeRequests.length === 0 && approvedRequests.length === 0 && rejectedRequests.length === 0 && returnedRequests.length === 0 && (
          <div className="requests-tab-empty">No requests found</div>
        )}

        {approvedRequests.length > 0 && (
          <div className="approved-section-wrapper">
            <div className="section-divider"></div>
            <div className="approved-items-list">
              {approvedRequests.map((req, index) => (
                <LazyItem key={req.id} estimatedHeight="54px">
                  <RequestItem 
                    request={req} 
                    index={index}
                    isExpanded={expandedId === req.id}
                    onToggle={id => setExpandedId(expandedId === id ? null : id)}
                    onApprove={handleApprove} 
                    onReject={handleReject}
                  />
                </LazyItem>
              ))}
            </div>
          </div>
        )}

        {returnedRequests.length > 0 && (
          <div className="returned-section-wrapper">
            <button 
              className={`returned-toggle-btn ${showReturned ? 'active' : ''}`}
              onClick={() => setShowReturned(!showReturned)}
            >
              <span>Returned Requests ({returnedRequests.length})</span>
              <svg 
                className={`requests-tab-icon ${showReturned ? 'expanded' : ''}`} 
                viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <div ref={returnedRef} className="returned-items-container" style={{ height: 0, opacity: 0, overflow: 'hidden' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                {returnedRequests.map((req, index) => (
                  <LazyItem key={req.id} estimatedHeight="54px">
                    <RequestItem 
                      request={req} 
                      index={index}
                      isExpanded={expandedId === req.id}
                      onToggle={id => setExpandedId(expandedId === id ? null : id)}
                      onApprove={handleApprove} 
                      onReject={handleReject}
                    />
                  </LazyItem>
                ))}
              </div>
            </div>
          </div>
        )}

        {rejectedRequests.length > 0 && (
          <div className="rejected-section-wrapper">
            <button 
              className={`rejected-toggle-btn ${showRejected ? 'active' : ''}`}
              onClick={() => setShowRejected(!showRejected)}
            >
              <span>Rejected Requests ({rejectedRequests.length})</span>
              <svg 
                className={`requests-tab-icon ${showRejected ? 'expanded' : ''}`} 
                viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <div ref={rejectedRef} className="rejected-items-container" style={{ height: 0, opacity: 0, overflow: 'hidden' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                {rejectedRequests.map((req, index) => (
                  <LazyItem key={req.id} estimatedHeight="54px">
                    <RequestItem 
                      request={req} 
                      index={index}
                      isExpanded={expandedId === req.id}
                      onToggle={id => setExpandedId(expandedId === id ? null : id)}
                      onApprove={handleApprove} 
                      onReject={handleReject}
                    />
                  </LazyItem>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
