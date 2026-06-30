import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import LandingView from './components/features/LandingView';
import AuthView from './components/features/AuthView';
import CategoriaDetalleView from './components/features/CategoriaDetalleView';
import ExamenRunnerView from './components/features/ExamenRunnerView';
import DashboardCreadorView from './components/features/DashboardCreadorView';
import ExamenEditorView from './components/features/ExamenEditorView';

// Componente para rutas protegidas
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  
  if (!isAuthenticated) {
    return <Navigate to="/auth/login" replace />;
  }
  
  return <>{children}</>;
};

// Componente para redirigir si ya está autenticado
const PublicRoute = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }
  
  return <>{children}</>;
};

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Rutas públicas */}
        <Route path="/" element={<LandingView />} />
        <Route 
          path="/auth/login" 
          element={
            <PublicRoute>
              <AuthView mode="login" />
            </PublicRoute>
          } 
        />
        <Route 
          path="/auth/register" 
          element={
            <PublicRoute>
              <AuthView mode="register" />
            </PublicRoute>
          } 
        />
        
        {/* Rutas públicas de contenido */}
        <Route path="/categorias/:slug" element={<CategoriaDetalleView />} />
        
        {/* Rutas protegidas */}
        <Route 
          path="/dashboard" 
          element={
            <ProtectedRoute>
              <DashboardCreadorView />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/dashboard/crear-examen" 
          element={
            <ProtectedRoute>
              <ExamenEditorView />
            </ProtectedRoute>
          } 
        />
        
        {/* Ruta de examen (puede ser pública o protegida según el requisito) */}
        <Route path="/examenes/:slug/rendir" element={<ExamenRunnerView />} />
        
        {/* Ruta por defecto */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRouter;
