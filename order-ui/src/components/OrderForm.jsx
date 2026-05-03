import { useState, useEffect } from "react";
import "./OrderForm.css";

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const COUNTRY_CODES = [
  { code: "+1", country: "US" },
  { code: "+44", country: "UK" },
  { code: "+91", country: "IN" },
  { code: "+61", country: "AU" },
  { code: "+81", country: "JP" },
  { code: "+49", country: "DE" },
  { code: "+33", country: "FR" },
  { code: "+86", country: "CN" },
  { code: "+82", country: "KR" },
  { code: "+971", country: "UAE" },
  { code: "+65", country: "SG" },
  { code: "+55", country: "BR" },
  { code: "+52", country: "MX" },
  { code: "+39", country: "IT" },
  { code: "+34", country: "ES" },
  { code: "+7", country: "RU" },
  { code: "+27", country: "ZA" },
  { code: "+234", country: "NG" },
  { code: "+62", country: "ID" },
  { code: "+60", country: "MY" },
];

export default function OrderForm({ user }) {
  const [products, setProducts] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [quantity, setQuantity] = useState("");
  const [address, setAddress] = useState("");
  const [countryCode, setCountryCode] = useState(user?.countryCode || "+91");
  const [mobile, setMobile] = useState(user?.mobileNumber || "");
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (type, message) => {
    setToast({ type, message });
    setTimeout(() => setToast(null), 4000);
  };

  useEffect(() => {
    fetch(`${BASE_URL}/products`)
      .then((res) => res.json())
      .then((data) => setProducts(data))
      .catch(() => showToast("error", "Failed to load products"));
  }, []);

  const handleProductChange = (e) => {
    const productId = e.target.value;
    const product = products.find((p) => p.id === productId);
    setSelectedProduct(product || null);
    setQuantity("");
  };

  const calculatedAmount =
    selectedProduct && quantity
      ? (selectedProduct.sellingPrice * parseInt(quantity)).toFixed(2)
      : "0.00";

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!selectedProduct) {
      showToast("error", "Please select a product");
      return;
    }
    if (!quantity || parseInt(quantity) <= 0) {
      showToast("error", "Please enter a valid quantity");
      return;
    }
    if (parseInt(quantity) > selectedProduct.availableQty) {
      showToast(
        "error",
        `Max available quantity is ${selectedProduct.availableQty}`,
      );
      return;
    }
    if (!address.trim()) {
      showToast("error", "Please enter a delivery address");
      return;
    }
    if (!mobile.trim() || mobile.trim().length < 10) {
      showToast("error", "Please enter a valid mobile number");
      return;
    }

    setLoading(true);

    // Combine country code and mobile: "+91-9876543210"
    const fullMobile = `${countryCode}-${mobile.trim()}`;

    try {
      const res = await fetch(`${BASE_URL}/orders`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: user.email,
          productId: selectedProduct.id,
          quantity: parseInt(quantity),
          address: address.trim(),
          mobileNumber: fullMobile,
        }),
      });

      const data = await res.json();

      if (res.ok) {
        showToast("success", "Order placed successfully!");
        setProducts((prev) =>
          prev.map((p) =>
            p.id === selectedProduct.id
              ? { ...p, availableQty: p.availableQty - parseInt(quantity) }
              : p,
          ),
        );
        setSelectedProduct((prev) =>
          prev
            ? { ...prev, availableQty: prev.availableQty - parseInt(quantity) }
            : null,
        );
        setQuantity("");
        setAddress("");
      } else {
        showToast("error", data.message || "Something went wrong");
      }
    } catch {
      showToast("error", "Failed to connect to server");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="order-page">
      <div className="blob blob-1" />
      <div className="blob blob-2" />
      <div className="blob blob-3" />

      {toast && (
        <div className={`toast toast-${toast.type}`}>
          <span className="toast-icon">
            {toast.type === "success" ? "✓" : "✕"}
          </span>
          <span>{toast.message}</span>
        </div>
      )}

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
            Fill in the details to create a new order
          </p>
        </div>

        {/* User ID (Read-only) */}
        <div className="form-group">
          <label className="form-label">User</label>
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
              className="form-input readonly"
              type="text"
              value={user.email}
              readOnly
            />
          </div>
        </div>

        {/* Product Dropdown */}
        <div className="form-group">
          <label className="form-label">Product</label>
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
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
            </svg>
            <select
              className="form-input form-select"
              onChange={handleProductChange}
              defaultValue=""
            >
              <option value="" disabled>
                Select a product
              </option>
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} — ${p.sellingPrice}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Product Info */}
        {selectedProduct && (
          <div className="product-info">
            <div className="info-chip">
              <span className="info-label">Price</span>
              <span className="info-value">
                ${selectedProduct.sellingPrice}
              </span>
            </div>
            <div className="info-chip">
              <span className="info-label">In Stock</span>
              <span
                className={`info-value ${selectedProduct.availableQty <= 5 ? "low-stock" : ""}`}
              >
                {selectedProduct.availableQty}
              </span>
            </div>
          </div>
        )}

        {/* Quantity */}
        <div className="form-group">
          <label className="form-label">Quantity</label>
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
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            <input
              className="form-input"
              type="number"
              min="1"
              max={selectedProduct?.availableQty || 0}
              placeholder={
                selectedProduct
                  ? `Max ${selectedProduct.availableQty}`
                  : "Select a product first"
              }
              value={quantity}
              onChange={(e) => {
                const val = parseInt(e.target.value);
                if (val > selectedProduct?.availableQty) {
                  showToast(
                    "error",
                    `Max available: ${selectedProduct.availableQty}`,
                  );
                  return;
                }
                setQuantity(e.target.value);
              }}
              disabled={!selectedProduct}
            />
          </div>
        </div>

        {/* Total Amount (Read-only) */}
        <div className="form-group">
          <label className="form-label">Total Amount</label>
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
              className="form-input readonly amount-highlight"
              type="text"
              value={`$ ${calculatedAmount}`}
              readOnly
            />
          </div>
        </div>

        {/* Address */}
        <div className="form-group">
          <label className="form-label">Delivery Address</label>
          <div className="input-wrapper">
            <svg
              className="input-icon textarea-icon"
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
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
              <circle cx="12" cy="10" r="3" />
            </svg>
            <textarea
              className="form-input form-textarea"
              placeholder="Enter your full delivery address"
              rows="3"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
            />
          </div>
        </div>

        {/* Mobile Number with Country Code */}
        <div className="form-group">
          <label className="form-label">Mobile Number</label>
          <div className="phone-input-row">
            <div className="country-code-wrapper">
              <select
                className="country-code-select"
                value={countryCode}
                onChange={(e) => setCountryCode(e.target.value)}
              >
                {COUNTRY_CODES.map((c) => (
                  <option key={c.code} value={c.code}>
                    {c.country} {c.code}
                  </option>
                ))}
              </select>
            </div>
            <div className="input-wrapper mobile-input-wrapper">
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
                <rect x="5" y="2" width="14" height="20" rx="2" ry="2" />
                <line x1="12" y1="18" x2="12.01" y2="18" />
              </svg>
              <input
                className="form-input"
                type="tel"
                placeholder="9876543210"
                maxLength={10}
                value={mobile}
                onChange={(e) => {
                  const val = e.target.value.replace(/[^0-9]/g, "");
                  setMobile(val);
                }}
              />
            </div>
          </div>
        </div>

        {/* Submit */}
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
