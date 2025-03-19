import { createLazyFileRoute } from '@tanstack/react-router';

export const Route = createLazyFileRoute('/')({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <div>
      <Exampe>
        <div>Example</div>
      </Exampe>
    </div>
  );
}

function Exampe({ children }: { children: React.ReactNode }) {
  return <div>{children}</div>;
}
