import Link from 'next/link';
import { CATEGORIES, PLATFORMS } from '@/lib/constants';

export function Footer() {
  return (
    <footer className="border-t bg-muted/50">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-sm font-bold text-primary-foreground">
                A
              </div>
              <span className="text-lg font-bold">Atlas Deals</span>
            </div>
            <p className="mt-3 text-sm text-muted-foreground">
              As melhores promoções de tecnologia em um só lugar. Monitoramos os preços das principais lojas para você economizar.
            </p>
          </div>
          <div>
            <h3 className="mb-4 text-sm font-semibold">Categorias</h3>
            <ul className="space-y-2">
              {CATEGORIES.map((cat) => (
                <li key={cat.slug}>
                  <Link
                    href={`/categorias/${cat.slug}`}
                    className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                  >
                    {cat.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
          <div>
            <h3 className="mb-4 text-sm font-semibold">Acompanhe</h3>
            <ul className="space-y-2">
              {PLATFORMS.slice(0, 4).map((platform) => (
                <li key={platform.id}>
                  <Link
                    href="#"
                    className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                  >
                    {platform.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
          <div>
            <h3 className="mb-4 text-sm font-semibold">Newsletter</h3>
            <p className="mb-3 text-sm text-muted-foreground">
              Receba as melhores ofertas no seu email.
            </p>
            <form className="flex gap-2">
              <input
                type="email"
                placeholder="seu@email.com"
                className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              />
              <button
                type="submit"
                className="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
              >
                OK
              </button>
            </form>
          </div>
        </div>
        <div className="mt-8 border-t pt-8 text-center text-sm text-muted-foreground">
          &copy; {new Date().getFullYear()} Atlas Deals. Todos os direitos reservados.
        </div>
      </div>
    </footer>
  );
}
