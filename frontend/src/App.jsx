import { useState } from 'react'
import './index.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      <header>
        <div className="logo">Homelander</div>
      </header>
      <main>
        <p>This is React template powered by Vite.</p>
        <button onClick={() => setCount((count) => count + 1)}>
          Count is {count}
        </button>
      </main>
    </>
  )
}

export default App
