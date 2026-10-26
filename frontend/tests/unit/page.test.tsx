import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import HomePage from '@/app/page';
import '@testing-library/jest-dom';

describe('HomePage Placeholder Smoke Test', () => {
  it('renders ClassicStore heading', () => {
    render(<HomePage />);
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
      'ClassicStore'
    );
  });
});
