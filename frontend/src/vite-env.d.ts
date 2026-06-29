/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string
  // Agregá acá otras variables que definas en tu .env en el futuro
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}