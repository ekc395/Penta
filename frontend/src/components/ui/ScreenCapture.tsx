import { useRef } from "react";

export function ScreenCapture() {
  const videoRef = useRef<HTMLVideoElement>(null);

  async function startCapture() {
    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: false
      });

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }

      // Capture frame every 2 seconds
      setInterval(() => {
        if (!videoRef.current) return;

        const frame = captureFrame(videoRef.current);
        sendFrame(frame);
      }, 2000);

    } catch (err) {
      console.error("Screen capture error:", err);
    }
  }

  function captureFrame(video: HTMLVideoElement): string {
    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const ctx = canvas.getContext("2d");
    if (!ctx) return "";

    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    return canvas.toDataURL("image/png");
  }

  // IN PROGRESS
  async function sendFrame(image: string) {
    try {
      await fetch("http://localhost:3001/api/process-frame", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ image })
      });
    } catch (error) {
      console.error("Error sending frame:", error);
    }
  }

  return (
    <div className="flex flex-col mb-8">
        <div className="text-2xl font-semibold text-gray-100 mb-6">
            <label>Live Draft Analysis</label>
        </div>
        <video
        ref={videoRef}
        autoPlay
        playsInline
        className="w-full rounded border"
        />
        <button onClick={startCapture} className="px-4 py-2 bg-green-700 text-white rounded">
            Share Screen
        </button>
    </div>
  );
}
