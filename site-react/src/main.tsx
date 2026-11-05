import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '@fontsource/inter/400.css';
import '@fontsource/inter/500.css';
import '@fontsource/jetbrains-mono/500.css';
import 'virtual:dui-tokens.css';
import './dui.css';
import { App } from './App';

const root = document.getElementById('root');
if (!root) throw new Error('index.html must have a #root element');

createRoot(root).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
