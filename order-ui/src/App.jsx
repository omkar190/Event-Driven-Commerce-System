import { useState } from "react";
import AuthPage from "./components/AuthPage";
import OrderForm from "./components/OrderForm";

export default function App() {
  const [user, setUser] = useState(null);

  if (!user) {
    return <AuthPage onLogin={setUser} />;
  }

  return <OrderForm user={user} />;
}
