export const STORE_LOGOS: Record<string, string> = {
  'Shopee': '/images/stores/shopee.svg',
  'Amazon': '/images/stores/amazon.svg',
  'KaBuM!': '/images/stores/kabum.svg',
  'Magalu': '/images/stores/magalu.svg',
  'Mercado Livre': '/images/stores/mercadolivre.svg',
  'Americanas': '/images/stores/americanas.svg',
  'Pichau': '/images/stores/pichau.svg',
  'Terabyte': '/images/stores/terabyte.svg',
  'AliExpress': '/images/stores/aliexpress.svg',
};

export const CATEGORY_ICONS: Record<string, string> = {
  'hardware': 'cpu',
  'notebooks': 'laptop',
  'monitores': 'monitor',
  'perifericos': 'keyboard',
  'gpus': 'gpu',
  'armazenamento': 'hard-drive',
  'memoria': 'memory-stick',
  'audio': 'headphones',
  'gaming': 'gamepad',
  'smartphones': 'smartphone',
  'tv': 'tv',
  'escritorio': 'office-chair',
};

export const CATEGORY_LABELS: Record<string, string> = {
  'hardware': 'Hardware',
  'notebooks': 'Notebooks',
  'monitores': 'Monitores',
  'perifericos': 'Periféricos',
  'gpus': 'Placas de Vídeo',
  'armazenamento': 'Armazenamento',
  'memoria': 'Memória RAM',
  'audio': 'Áudio',
  'gaming': 'Gaming',
  'smartphones': 'Smartphones',
  'tv': 'TV & Home Theater',
  'escritorio': 'Escritório',
};

export const CATEGORIES = Object.entries(CATEGORY_LABELS).map(([slug, label]) => ({
  slug,
  label,
}));

export const PLATFORMS = [
  { id: 'telegram', label: 'Telegram', color: '#26a5e4' },
  { id: 'whatsapp', label: 'WhatsApp', color: '#25d366' },
  { id: 'twitter', label: 'Twitter / X', color: '#1da1f2' },
  { id: 'instagram', label: 'Instagram', color: '#e4405f' },
  { id: 'facebook', label: 'Facebook', color: '#1877f2' },
  { id: 'discord', label: 'Discord', color: '#5865f2' },
  { id: 'site', label: 'Site', color: '#6366f1' },
];

export const STORE_THEMES: Record<string, { primary: string; secondary: string }> = {
  'Shopee': { primary: '#ee4d2d', secondary: '#ff7337' },
  'Amazon': { primary: '#ff9900', secondary: '#232f3e' },
  'KaBuM!': { primary: '#c4122e', secondary: '#1a1a1a' },
  'Magalu': { primary: '#005c99', secondary: '#00a1fc' },
  'Mercado Livre': { primary: '#ffe600', secondary: '#333333' },
};
