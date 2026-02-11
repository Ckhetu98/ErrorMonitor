import React from 'react'
import Navbar from './Navbar'
import Footer from './Footer'

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-300 flex flex-col">
      <Navbar />
      <main className="flex-1 p-6 pb-20 min-h-[80vh]">
        {children}
      </main>
      <Footer />
    </div>
  )
}

export default Layout