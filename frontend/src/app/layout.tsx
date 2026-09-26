import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { Header } from '@/components/layout/header';
import { Footer } from '@/components/layout/footer';
import { MobileNav } from '@/components/layout/mobile-nav';
import { AuthProvider } from '@/providers/auth-provider';
import { Toaster } from 'sonner';

const inter = Inter({ subsets: ['latin'], variable: '--font-inter' });

export const metadata: Metadata = {
  title: {
    default: 'Atlas Deals - Promoções de Tecnologia',
    template: '%s | Atlas Deals',
  },
  description: 'As melhores promoções de tecnologia monitoradas em tempo real. Compare preços, veja histórico e economize nas suas compras.',
  keywords: ['promoções', 'tecnologia', 'ofertas', 'descontos', 'comparador de preços'],
  openGraph: {
    type: 'website',
    locale: 'pt_BR',
    siteName: 'Atlas Deals',
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <body className={`${inter.variable} font-sans antialiased`}>
        <AuthProvider>
          <div className="flex min-h-screen flex-col">
            <Header />
            <main className="flex-1">{children}</main>
            <Footer />
            <MobileNav />
          </div>
          <Toaster
            position="top-right"
            richColors
            closeButton
          />
        </AuthProvider>
      </body>
    </html>
  );
}
