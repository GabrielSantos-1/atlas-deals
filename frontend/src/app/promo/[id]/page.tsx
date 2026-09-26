import { Metadata } from 'next';
import { notFound } from 'next/navigation';
import { PromoDetail } from '@/components/promo/promo-detail';
import { api } from '@/lib/api';
import type { Promotion } from '@/types';

interface Props {
  params: { id: string };
}

async function getPromotion(id: string): Promise<Promotion | null> {
  try {
    return await api.getPromotion(id);
  } catch {
    return null;
  }
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const promotion = await getPromotion(params.id);
  if (!promotion) return { title: 'Promoção não encontrada' };

  return {
    title: `${promotion.title} - ${promotion.currentPrice}`,
    description: `${promotion.title} por apenas ${promotion.currentPrice} na ${promotion.storeName}. ${promotion.discountPercentage}% de desconto!`,
    openGraph: {
      title: `${promotion.title} - Atlas Deals`,
      description: `${promotion.discountPercentage}% OFF - ${promotion.currentPrice}`,
      images: [{ url: promotion.imageUrl }],
    },
  };
}

export default async function PromoDetailPage({ params }: Props) {
  const promotion = await getPromotion(params.id);

  if (!promotion) {
    notFound();
  }

  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: promotion.title,
    image: promotion.imageUrl,
    description: `${promotion.title} com ${promotion.discountPercentage}% de desconto`,
    offers: {
      '@type': 'Offer',
      price: promotion.currentPrice,
      priceCurrency: 'BRL',
      availability: promotion.inStock ? 'https://schema.org/InStock' : 'https://schema.org/OutOfStock',
      seller: {
        '@type': 'Organization',
        name: promotion.storeName,
      },
    },
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <PromoDetail promotion={promotion} />
    </div>
  );
}
