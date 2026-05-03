import { useState } from "react";
import "./OrderForm.css";

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export default function OrderForm() {
  const [userId, setUserId] = useState("");
  const [amount, setAmount] = useState("");
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (type, message) => {
    setToast({ type, message });
    setTimeout(() => setToast(null), 4000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!userId.trim() || !amount) {
      showToast("error", "Please fill in all fields");
      return;
    }

    if (parseFloat(amount) <= 0) {
      showToast("error", "Amount must be greater than 0");
      return;
    }

    setLoading(true);

    try {
      const res = await fetch(`${BASE_URL}/orders`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: userId.trim(),
          amount: parseFloat(amount),
        }),
      });

      if (res.ok) {
        showToast("success", "Order placed successfully!");
        setUserId("");
        setAmount("");
      } else {
        const data = await res.json().catch(() => null);
        showToast("error", data?.message || "Something went wrong");
      }
    } catch {
      showToast("error", "Failed to connect to server");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="order-page">
      {/* Background blobs */}
      <div className="blob blob-1" />
      <div className="blob blob-2" />
      <div className="blob blob-3" />

      {/* Toast notification */}
      {toast && (
        <div className={`toast toast-${toast.type}`}>
          <span className="toast-icon">
            {toast.type === "success" ? "✓" : "✕"}
          </span>
          <span>{toast.message}</span>
        </div>
      )}

      {/* Card */}
      <form className="order-card" onSubmit={handleSubmit}>
        <div className="card-header">
          <div className="card-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="28"
              height="28"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="9" cy="21" r="1" />
              <circle cx="20" cy="21" r="1" />
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
            </svg>
          </div>
          <h1 className="card-title">Place Order</h1>
          <p className="card-subtitle">
            Fill in the details below to create a new order
          </p>
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="userId">
            User ID
          </label>
          <div className="input-wrapper">
            <svg
              className="input-icon"
              xmlns="http://www.w3.org/2000/svg"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            <input
              id="userId"
              className="form-input"
              type="text"
              placeholder="Enter user ID"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
            />
          </div>
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="amount">
            Amount
          </label>
          <div className="input-wrapper">
            <svg
              className="input-icon"
              xmlns="http://www.w3.org/2000/svg"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <line x1="12" y1="1" x2="12" y2="23" />
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
            </svg>
            <input
              id="amount"
              className="form-input"
              type="number"
              step="0.01"
              min="0.01"
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
            />
          </div>
        </div>

        <button
          className={`submit-btn ${loading ? "loading" : ""}`}
          type="submit"
          disabled={loading}
        >
          {loading ? (
            <span className="spinner" />
          ) : (
            <>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              Place Order
            </>
          )}
        </button>
      </form>
    </div>
  );
}
