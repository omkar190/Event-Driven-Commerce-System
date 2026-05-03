import { useState } from "react";
import "./AuthPage.css";

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

export default function AuthPage({ onLogin }) {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [countryCode, setCountryCode] = useState("+91");
  const [mobile, setMobile] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (type, message) => {
    setToast({ type, message });
    setTimeout(() => setToast(null), 4000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!email.trim() || !password.trim()) {
      showToast("error", "Please fill in all fields");
      return;
    }

    if (password.length < 6) {
      showToast("error", "Password must be at least 6 characters");
      return;
    }

    if (!isLogin && (!mobile.trim() || mobile.trim().length < 10)) {
      showToast("error", "Please enter a valid mobile number");
      return;
    }

    setLoading(true);

    const endpoint = isLogin ? "/auth/login" : "/auth/signup";
    const body = isLogin
      ? { email: email.trim(), password }
      : {
          email: email.trim(),
          password,
          countryCode,
          mobileNumber: mobile.trim(),
        };

    try {
      const res = await fetch(`${BASE_URL}${endpoint}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      const data = await res.json();

      if (res.ok) {
        showToast("success", data.message || "Success!");
        if (isLogin && onLogin) {
          setTimeout(() => onLogin(data), 1000);
        }
        if (!isLogin) {
          setTimeout(() => {
            setIsLogin(true);
            setPassword("");
            setMobile("");
          }, 1500);
        }
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
    <div className="auth-page">
      <div className="auth-blob auth-blob-1" />
      <div className="auth-blob auth-blob-2" />
      <div className="auth-blob auth-blob-3" />

      {toast && (
        <div className={`auth-toast auth-toast-${toast.type}`}>
          <span className="auth-toast-icon">
            {toast.type === "success" ? "✓" : "✕"}
          </span>
          <span>{toast.message}</span>
        </div>
      )}

      <form className="auth-card" onSubmit={handleSubmit}>
        <div className="auth-header">
          <div className="auth-icon">
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
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          </div>
          <h1 className="auth-title">
            {isLogin ? "Welcome Back" : "Create Account"}
          </h1>
          <p className="auth-subtitle">
            {isLogin
              ? "Sign in to continue to your dashboard"
              : "Sign up to get started with your account"}
          </p>
        </div>

        <div className="auth-toggle">
          <button
            type="button"
            className={`toggle-btn ${isLogin ? "active" : ""}`}
            onClick={() => {
              setIsLogin(true);
              setPassword("");
              setMobile("");
            }}
          >
            Login
          </button>
          <button
            type="button"
            className={`toggle-btn ${!isLogin ? "active" : ""}`}
            onClick={() => {
              setIsLogin(false);
              setPassword("");
              setMobile("");
            }}
          >
            Sign Up
          </button>
        </div>

        {/* Email */}
        <div className="auth-form-group">
          <label className="auth-label" htmlFor="email">
            Email
          </label>
          <div className="auth-input-wrapper">
            <svg
              className="auth-input-icon"
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
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
              <polyline points="22,6 12,13 2,6" />
            </svg>
            <input
              id="email"
              className="auth-input"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
        </div>

        {/* Password */}
        <div className="auth-form-group">
          <label className="auth-label" htmlFor="password">
            Password
          </label>
          <div className="auth-input-wrapper">
            <svg
              className="auth-input-icon"
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
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
            <input
              id="password"
              className="auth-input"
              type={showPassword ? "text" : "password"}
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? (
                <svg
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
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
                  <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
              ) : (
                <svg
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
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              )}
            </button>
          </div>
        </div>

        {/* Mobile Number - Only on Sign Up */}
        {!isLogin && (
          <div className="auth-form-group">
            <label className="auth-label">Mobile Number</label>
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
              <div className="auth-input-wrapper mobile-input-wrapper">
                <svg
                  className="auth-input-icon"
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
                  className="auth-input"
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
        )}

        {/* Submit */}
        <button
          className={`auth-submit ${loading ? "loading" : ""}`}
          type="submit"
          disabled={loading}
        >
          {loading ? (
            <span className="auth-spinner" />
          ) : (
            <>
              {isLogin ? (
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
                  <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
                  <polyline points="10 17 15 12 10 7" />
                  <line x1="15" y1="12" x2="3" y2="12" />
                </svg>
              ) : (
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
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="8.5" cy="7" r="4" />
                  <line x1="20" y1="8" x2="20" y2="14" />
                  <line x1="23" y1="11" x2="17" y2="11" />
                </svg>
              )}
              {isLogin ? "Sign In" : "Create Account"}
            </>
          )}
        </button>
      </form>
    </div>
  );
}
