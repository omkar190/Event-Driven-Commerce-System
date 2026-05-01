export default function HomePage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white px-6">
      <div className="text-center max-w-2xl">
        {/* Main Card */}
        <div className="rounded-3xl p-10 bg-white/10 backdrop-blur-lg border border-white/20 shadow-2xl">
          <h1 className="text-5xl font-bold mb-4">
            Order Service Dashboard
          </h1>

          <p className="text-lg text-slate-300 mb-6">
            A simple backend service home page
          </p>

          {/* Message */}
          <div className="inline-block px-6 py-3 rounded-full bg-black/30 border border-white/10 text-green-300 font-medium">
            Order Service is up and running 🚀
          </div>
        </div>

        {/* Footer */}
        <p className="mt-6 text-sm text-slate-400">
          Built for internal backend monitoring
        </p>
      </div>
    </div>
  );
}
