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

const SCREEN = {
  AUTH: "AUTH",
  OTP: "OTP",
};

export default function AuthPage({ onLogin }) {
  const [screen, setScreen] = useState(SCREEN.AUTH);
  const [isLogin, setIsLogin] = useState(true);

  // Auth form
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [countryCode, setCountryCode] = useState("+91");
  const [mobile, setMobile] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  // OTP
  const [otp, setOtp] = useState(["", "", "", "", "", ""]);
  const [otpEmail, setOtpEmail] = useState("");
  const [otpType, setOtpType] = useState("");
  const [resendCooldown, setResendCooldown] = useState(0);

  // Inactive account resend
  const [showResendVerification, setShowResendVerification] = useState(false);

  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (type, message) => {
    setToast({ type, message });
    setTimeout(() => setToast(null), 4000);
  };

  const startCooldown = () => {
    setResendCooldown(30);
    const interval = setInterval(() => {
      setResendCooldown((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  // OTP input handling
  const handleOtpChange = (index, value) => {
    if (!/^\d*/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value.slice(-1);
    setOtp(newOtp);

    if (value && index < 5) {
      document.getElementById(`otp-${index + 1}`)?.focus();
    }
  };

  const handleOtpKeyDown = (index, e) => {
    if (e.key === "Backspace" && !otp[index] && index > 0) {
      document.getElementById(`otp-${index - 1}`)?.focus();
    }
  };

  const handleOtpPaste = (e) => {
    const pasted = e.clipboardData
      .getData("text")
      .replace(/\D/g, "")
      .slice(0, 6);
    if (pasted.length === 6) {
      setOtp(pasted.split(""));
      document.getElementById("otp-5")?.focus();
    }
  };

  // Go to OTP screen
  const goToOtpScreen = (email, type) => {
    setOtpEmail(email);
    setOtpType(type);
    setOtp(["", "", "", "", "", ""]);
    setScreen(SCREEN.OTP);
    setShowResendVerification(false);
    startCooldown();
    console.log("Outside goToOtpScreen");
  };

  // SIGNUP
  const handleSignup = async () => {
    if (!email.trim() || !password.trim()) {
      showToast("error", "Please fill in all fields");
      return;
    }
    if (password.length < 6) {
      showToast("error", "Password must be at least 6 characters");
      return;
    }
    if (!mobile.trim() || mobile.trim().length < 10) {
      showToast("error", "Please enter a valid mobile number");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${BASE_URL}/auth/signup`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email.trim(),
          password,
          countryCode,
          mobileNumber: mobile.trim(),
        }),
      });

      const data = await res.json();

      if (res.ok) {
        if (data.message?.includes("OTP")) {
          // OTP is ON - go to OTP screen
          showToast("success", data.message);
          goToOtpScreen(email.trim(), "SIGNUP");
        } else {
          // OTP is OFF - account created, go to login
          showToast("success", data.message);
          setTimeout(() => {
            setIsLogin(true);
            setPassword("");
            setMobile("");
          }, 1500);
        }
      } else {
        showToast("error", data.message || "Signup failed");
      }
    } catch {
      showToast("error", "Failed to connect to server");
    } finally {
      setLoading(false);
    }
  };

  // LOGIN
  const handleLogin = async () => {
    if (!email.trim() || !password.trim()) {
      showToast("error", "Please fill in all fields");
      return;
    }

    setLoading(true);
    setShowResendVerification(false);

    try {
      const res = await fetch(`${BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email.trim(),
          password,
        }),
      });

      const data = await res.json();

      if (res.ok) {
        if (data.otpRequired) {
          // OTP is ON - go to OTP screen
          showToast("success", data.message);
          goToOtpScreen(email.trim(), "LOGIN");
        } else {
          // OTP is OFF - login directly (got user data back)
          showToast("success", "Login successful!");
          setTimeout(() => onLogin(data), 1000);
        }
      } else {
        if (data.message?.includes("not activated")) {
          setShowResendVerification(true);
        }
        showToast("error", data.message || "Login failed");
      }
    } catch {
      showToast("error", "Failed to connect to server");
    } finally {
      setLoading(false);
    }
  };

  // RESEND VERIFICATION (from login screen for inactive accounts)
  const handleResendVerification = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${BASE_URL}/auth/resend-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email.trim(),
          type: "SIGNUP",
        }),
      });

      const data = await res.json();

      if (res.ok) {
        showToast("success", "Verification email sent!");
        goToOtpScreen(email.trim(), "SIGNUP");
      } else {
        showToast("error", data.message || "Failed to send");
      }
    } catch {
      showToast("error", "Failed to connect to server");
    } finally {
      setLoading(false);
    }
  };

  // VERIFY OTP
  const handleVerifyOtp = async () => {
    const otpString = otp.join("");
    if (otpString.length !== 6) {
      showToast("error", "Please enter all 6 digits");
      return;
    }

    setLoading(true);
    const endpoint =
      otpType === "SIGNUP" ? "/auth/verify-signup" : "/auth/verify-login";

    try {
      const res = await fetch(`${BASE_URL}${endpoint}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: otpEmail,
          otp: otpString,
        }),
      });

      const data = await res.json();

      if (res.ok) {
        if (otpType === "SIGNUP") {
          showToast("success", "Account verified! Please login.");
          setTimeout(() => {
            setScreen(SCREEN.AUTH);
            setIsLogin(true);
            setOtp(["", "", "", "", "", ""]);
            setPassword("");
          }, 1500);
        } else {
          showToast("success", "Login successful!");
          setTimeout(() => onLogin(data), 1000);
        }
      } else {
        showToast("error", data.message || "Invalid OTP");
        setOtp(["", "", "", "", "", ""]);
        document.getElementById("otp-0")?.focus();
      }
    } catch {
      showToast("error", "Failed to connect to server");
    } finally {
      setLoading(false);
    }
  };

  // RESEND OTP (from OTP screen)
  const handleResendOtp = async () => {
    if (resendCooldown > 0) return;

    setLoading(true);
    try {
      const res = await fetch(`${BASE_URL}/auth/resend-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: otpEmail,
          type: otpType,
        }),
      });

      const data = await res.json();

      if (res.ok) {
        showToast("success", "OTP resent! Check your email.");
        setOtp(["", "", "", "", "", ""]);
        startCooldown();
      } else {
        showToast("error", data.message || "Failed to resend");
      }
    } catch {
      showToast("error", "Failed to connect to server");
    } finally {
      setLoading(false);
    }
  };

  const handleAuthSubmit = (e) => {
    e.preventDefault();
    if (isLogin) handleLogin();
    else handleSignup();
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

      {/* ===== OTP SCREEN ===== */}
      {screen === SCREEN.OTP && (
        <div className="auth-card">
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
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                <polyline points="22,6 12,13 2,6" />
              </svg>
            </div>
            <h1 className="auth-title">Check your email</h1>
            <p className="auth-subtitle">We sent a 6-digit OTP to</p>
            <p className="otp-email">{otpEmail}</p>
          </div>

          <div className="otp-boxes" onPaste={handleOtpPaste}>
            {otp.map((digit, index) => (
              <input
                key={index}
                id={`otp-${index}`}
                className="otp-box"
                type="text"
                inputMode="numeric"
                maxLength={1}
                value={digit}
                onChange={(e) => handleOtpChange(index, e.target.value)}
                onKeyDown={(e) => handleOtpKeyDown(index, e)}
                autoFocus={index === 0}
              />
            ))}
          </div>

          <p className="otp-expiry">⏱ OTP expires in 10 minutes</p>

          <button
            className={`auth-submit ${loading ? "loading" : ""}`}
            onClick={handleVerifyOtp}
            disabled={loading}
          >
            {loading ? <span className="auth-spinner" /> : "Verify OTP"}
          </button>

          <div className="resend-row">
            <span>Didn't receive it? </span>
            <button
              className="resend-btn"
              onClick={handleResendOtp}
              disabled={resendCooldown > 0 || loading}
            >
              {resendCooldown > 0
                ? `Resend in ${resendCooldown}s`
                : "Resend OTP"}
            </button>
          </div>

          <button
            className="back-btn"
            onClick={() => {
              setScreen(SCREEN.AUTH);
              setOtp(["", "", "", "", "", ""]);
            }}
          >
            ← Back
          </button>
        </div>
      )}

      {/* ===== AUTH SCREEN ===== */}
      {screen === SCREEN.AUTH && (
        <form className="auth-card" onSubmit={handleAuthSubmit}>
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
                : "Sign up to get started"}
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
                setShowResendVerification(false);
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
                setShowResendVerification(false);
              }}
            >
              Sign Up
            </button>
          </div>

          {/* Email */}
          <div className="auth-form-group">
            <label className="auth-label">Email</label>
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
            <label className="auth-label">Password</label>
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

          {/* Mobile - Signup only */}
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
            ) : isLogin ? (
              "Continue"
            ) : (
              "Create Account"
            )}
          </button>

          {/* Resend Verification - Shows only when login fails due to inactive account */}
          {showResendVerification && (
            <button
              type="button"
              className="resend-verification-btn"
              onClick={handleResendVerification}
              disabled={loading}
            >
              📧 Resend Verification Email
            </button>
          )}
        </form>
      )}
    </div>
  );
}
