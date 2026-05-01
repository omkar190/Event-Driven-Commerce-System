export default function HomePage() {
  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Order Service 🚀</h1>
        <p style={styles.subtitle}>Backend is up and running.</p>

        <div style={styles.divider} />

        <p style={styles.message}>
          Use <span style={styles.highlight}>/orders</span> API to interact with
          the system.
        </p>

        <p style={styles.footer}>Built for performance. Designed for scale.</p>
      </div>
    </div>
  );
}

const styles = {
  container: {
    height: "100vh",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    background: "linear-gradient(135deg, #0f172a, #1e293b)",
    fontFamily: "system-ui, sans-serif",
  },
  card: {
    background: "#0f172a",
    padding: "40px 50px",
    borderRadius: "20px",
    boxShadow: "0 20px 50px rgba(0,0,0,0.6)",
    textAlign: "center",
    border: "1px solid rgba(255,255,255,0.05)",
  },
  title: {
    fontSize: "36px",
    marginBottom: "10px",
    color: "#38bdf8",
  },
  subtitle: {
    fontSize: "16px",
    color: "#94a3b8",
    marginBottom: "20px",
  },
  divider: {
    height: "1px",
    background: "rgba(255,255,255,0.1)",
    margin: "20px 0",
  },
  message: {
    fontSize: "18px",
    color: "#e2e8f0",
  },
  highlight: {
    color: "#22c55e",
    fontWeight: "bold",
  },
  footer: {
    marginTop: "20px",
    fontSize: "14px",
    color: "#64748b",
  },
};
