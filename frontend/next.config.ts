import type { NextConfig } from 'next';
import withBundleAnalyzer from '@next/bundle-analyzer';

const imageHosts = process.env.IMAGE_HOSTS
  ? process.env.IMAGE_HOSTS.split(',')
      .map((host) => host.trim())
      .filter(Boolean)
  : [];

const remotePatterns = imageHosts.map((hostname) => ({
  protocol: 'https' as const,
  hostname,
}));

const nextConfig: NextConfig = {
  reactStrictMode: true,
  poweredByHeader: false,
  compress: true,
  output: 'standalone',
  typedRoutes: true,

  images: {
    formats: ['image/avif', 'image/webp'],
    remotePatterns,
  },

  async headers() {
    return [
      {
        source: '/:path*',
        headers: [
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin',
          },
          {
            key: 'Permissions-Policy',
            value: 'camera=(), microphone=(), geolocation=()',
          },
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            // TODO(T10): Full Content Security Policy (CSP) headers will be implemented in T10.
            key: 'Content-Security-Policy',
            value: "frame-ancestors 'none';",
          },
        ],
      },
    ];
  },
};

const bundleAnalyzer = withBundleAnalyzer({
  enabled: process.env.ANALYZE === 'true',
});

export default bundleAnalyzer(nextConfig);
