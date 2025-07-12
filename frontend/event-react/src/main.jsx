import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router'

import './index.css'
import App from './App.jsx'
import AttendeeLandingPage from './pages/AttendeeLandingPage.jsx'
import LoginPage from './pages/LoginPage.jsx'

const router = createBrowserRouter([
  {
    path: '/',
    Component: AttendeeLandingPage,
  },
  {
    path: '/login',
    Component: LoginPage
  }
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
