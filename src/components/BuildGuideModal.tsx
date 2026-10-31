import React from 'react';
import { X, BookOpen, Terminal, CheckCircle2, ShieldCheck, Cpu, Database, Play } from 'lucide-react';

interface BuildGuideModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const BuildGuideModal: React.FC<BuildGuideModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div className="bg-[#121216] border border-[#26262E] w-full max-w-4xl max-h-[85vh] rounded-3xl flex flex-col shadow-2xl overflow-hidden text-white font-sans">
        {/* Header */}
        <div className="px-6 py-4 border-b border-[#202026] flex items-center justify-between bg-[#15151B]">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-[#00F0A8]/10 text-[#00F0A8]">
              <BookOpen className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">
                Android Studio Build & Verification Guide
              </h2>
              <p className="text-xs text-[#8E8E98]">
                Native compilation, Gradle targets, device deployment, and unit testing.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl bg-[#1D1D24] text-[#8E8E98] hover:text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6 text-sm text-[#B0B0B8]">
          {/* Step 1 */}
          <div className="space-y-2">
            <h3 className="text-white font-bold flex items-center gap-2">
              <span className="w-6 h-6 rounded-full bg-[#CCFF00] text-black font-black text-xs flex items-center justify-center">1</span>
              Environment & IDE Requirements
            </h3>
            <p className="text-xs text-[#8E8E98] leading-relaxed">
              AmpereX is built with Kotlin 2.0.21 and Jetpack Compose BOM 2024.11.00. Compatible with:
            </p>
            <ul className="text-xs text-[#8E8E98] list-disc list-inside space-y-1 pl-2">
              <li><strong>Android Studio:</strong> Hedgehog (2023.1.1), Iguana (2023.2.1), Jellyfish, Koala, or Ladybug (2024.2.1+)</li>
              <li><strong>JDK:</strong> Java 17 (bundled within Android Studio)</li>
              <li><strong>Android SDK:</strong> API Level 35 installed (minSdk: 26, targetSdk: 35)</li>
            </ul>
          </div>

          {/* Step 2 */}
          <div className="space-y-2">
            <h3 className="text-white font-bold flex items-center gap-2">
              <span className="w-6 h-6 rounded-full bg-[#CCFF00] text-black font-black text-xs flex items-center justify-center">2</span>
              Open & Sync in Android Studio
            </h3>
            <p className="text-xs text-[#8E8E98] leading-relaxed">
              1. Download the project archive or open the generated <code className="text-white bg-[#1A1A22] px-1.5 py-0.5 rounded">/AmpereX</code> directory.
            </p>
            <p className="text-xs text-[#8E8E98] leading-relaxed">
              2. In Android Studio, click <strong>File &gt; Open...</strong> and select the <code className="text-white bg-[#1A1A22] px-1.5 py-0.5 rounded">AmpereX</code> root folder.
            </p>
            <p className="text-xs text-[#8E8E98] leading-relaxed">
              3. Android Studio will automatically resolve Gradle wrapper 8.10.2 and compile dependencies.
            </p>
          </div>

          {/* Step 3 */}
          <div className="space-y-2">
            <h3 className="text-white font-bold flex items-center gap-2">
              <span className="w-6 h-6 rounded-full bg-[#CCFF00] text-black font-black text-xs flex items-center justify-center">3</span>
              Run Unit Tests via CLI
            </h3>
            <div className="bg-[#0A0A0D] border border-[#202026] rounded-xl p-3 font-mono text-xs text-[#CCFF00] flex items-center justify-between">
              <span>./gradlew test</span>
              <span className="text-[10px] text-[#656570]">Runs CalculatorsTest.kt</span>
            </div>
            <p className="text-xs text-[#8E8E98]">
              Verifies gross discharge separation, battery capacity health validations, charging ETA taper formulas, and deduplication logic.
            </p>
          </div>

          {/* Step 4 */}
          <div className="space-y-2">
            <h3 className="text-white font-bold flex items-center gap-2">
              <span className="w-6 h-6 rounded-full bg-[#CCFF00] text-black font-black text-xs flex items-center justify-center">4</span>
              Assemble & Install Debug APK
            </h3>
            <div className="bg-[#0A0A0D] border border-[#202026] rounded-xl p-3 font-mono text-xs text-[#00F0A8] flex items-center justify-between">
              <span>./gradlew assembleDebug</span>
              <span className="text-[10px] text-[#656570]">Outputs app-debug.apk</span>
            </div>
            <p className="text-xs text-[#8E8E98]">
              Output APK will be generated at <code className="text-white bg-[#1A1A22] px-1.5 py-0.5 rounded">app/build/outputs/apk/debug/app-debug.apk</code>.
            </p>
          </div>

          {/* Permissions note */}
          <div className="bg-[#15151B] border border-[#262630] rounded-2xl p-4 space-y-2">
            <div className="flex items-center gap-2 text-white font-semibold">
              <ShieldCheck className="w-4 h-4 text-[#CCFF00]" />
              <span>Permission Rationale for Play Store Review</span>
            </div>
            <p className="text-xs text-[#8E8E98] leading-relaxed">
              • <strong>PACKAGE_USAGE_STATS:</strong> Used exclusively by Apps screen to show foreground screen duration. No remote network calls are made.
            </p>
            <p className="text-xs text-[#8E8E98] leading-relaxed">
              • <strong>FOREGROUND_SERVICE_SPECIAL_USE:</strong> Allows opt-in real-time benchmarking during active charging tests without background sleep interruptions.
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-3 border-t border-[#202026] bg-[#121217] flex justify-end">
          <button
            onClick={onClose}
            className="py-2 px-5 rounded-xl bg-[#CCFF00] text-black font-bold text-xs"
          >
            Close Guide
          </button>
        </div>
      </div>
    </div>
  );
};
