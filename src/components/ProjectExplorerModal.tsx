import React, { useState } from 'react';
import { X, FileCode, Folder, Download, Copy, Check, Terminal, ExternalLink } from 'lucide-react';
import JSZip from 'jszip';
import { ANDROID_PROJECT_FILES, type AndroidFile } from '../data/projectFiles';

interface ProjectExplorerModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ProjectExplorerModal: React.FC<ProjectExplorerModalProps> = ({ isOpen, onClose }) => {
  const [selectedFile, setSelectedFile] = useState<AndroidFile>(ANDROID_PROJECT_FILES[0]);
  const [copied, setCopied] = useState(false);
  const [isZipping, setIsZipping] = useState(false);

  if (!isOpen) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedFile.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadZip = async () => {
    setIsZipping(true);
    try {
      const zip = new JSZip();

      // Add each file to the ZIP preserving folder hierarchy
      ANDROID_PROJECT_FILES.forEach((f) => {
        zip.file(f.path, f.content);
      });

      // Generate the zip blob
      const content = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(content);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'AmpereX-BatteryIntelligence-AndroidStudio.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to generate ZIP:', err);
    } finally {
      setIsZipping(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div className="bg-[#121216] border border-[#26262E] w-full max-w-5xl h-[85vh] rounded-3xl flex flex-col shadow-2xl overflow-hidden text-white font-sans">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-[#202026] flex items-center justify-between bg-[#15151B]">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-[#CCFF00]/10 text-[#CCFF00]">
              <Folder className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                Android Studio Project Explorer
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#1F1F26] text-[#CCFF00] font-mono border border-[#2F2F3B]">
                  Kotlin 2.0 • Jetpack Compose
                </span>
              </h2>
              <p className="text-xs text-[#8E8E98]">
                Browse complete native source files, Room schemas, DAOs, calculators, and Gradle configuration.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleDownloadZip}
              disabled={isZipping}
              className="py-2 px-3.5 rounded-xl bg-[#CCFF00] text-black font-bold text-xs flex items-center gap-2 hover:bg-[#b8e600] transition-colors shadow-sm disabled:opacity-50"
            >
              <Download className="w-4 h-4" />
              <span>{isZipping ? 'Packaging...' : 'Download Project (.ZIP)'}</span>
            </button>
            <button
              onClick={onClose}
              className="p-2 rounded-xl bg-[#1D1D24] text-[#8E8E98] hover:text-white transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Modal Body: Sidebar + Code Viewer */}
        <div className="flex-1 flex overflow-hidden">
          {/* File Tree Sidebar */}
          <div className="w-72 border-r border-[#202026] bg-[#0E0E12] overflow-y-auto p-3 space-y-1">
            <div className="text-[10px] font-bold uppercase tracking-wider text-[#656570] px-3 py-2">
              Project Structure
            </div>

            {ANDROID_PROJECT_FILES.map((file) => {
              const isSelected = selectedFile.path === file.path;
              return (
                <button
                  key={file.path}
                  onClick={() => setSelectedFile(file)}
                  className={`w-full text-left px-3 py-2 rounded-xl text-xs flex items-center justify-between transition-colors ${
                    isSelected
                      ? 'bg-[#1E1E26] text-[#CCFF00] font-semibold border border-[#2C2C38]'
                      : 'text-[#9E9EA7] hover:bg-[#15151B] hover:text-white'
                  }`}
                >
                  <div className="flex items-center gap-2 truncate">
                    <FileCode className={`w-3.5 h-3.5 shrink-0 ${isSelected ? 'text-[#CCFF00]' : 'text-[#656570]'}`} />
                    <span className="truncate">{file.name}</span>
                  </div>
                  <span className="text-[9px] uppercase px-1.5 py-0.5 rounded bg-[#181820] text-[#70707D]">
                    {file.category}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Code Viewer Panel */}
          <div className="flex-1 flex flex-col bg-[#0A0A0D] overflow-hidden">
            {/* File Path & Copy Bar */}
            <div className="px-5 py-2.5 border-b border-[#1C1C22] flex items-center justify-between bg-[#0F0F14] text-xs">
              <span className="font-mono text-[#8E8E98]">{selectedFile.path}</span>
              <button
                onClick={handleCopy}
                className="flex items-center gap-1.5 text-xs text-[#8E8E98] hover:text-[#CCFF00] transition-colors py-1 px-2.5 rounded-lg bg-[#181820]"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-[#CCFF00]" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copied ? 'Copied' : 'Copy Code'}</span>
              </button>
            </div>

            {/* Code Content */}
            <div className="flex-1 overflow-auto p-5 font-mono text-xs leading-relaxed text-[#D0D0D8]">
              <pre className="whitespace-pre">{selectedFile.content}</pre>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-3 border-t border-[#202026] bg-[#121217] flex items-center justify-between text-xs text-[#7A7A88]">
          <div className="flex items-center gap-2">
            <Terminal className="w-3.5 h-3.5 text-[#CCFF00]" />
            <span>Open directory in Android Studio Ladybug/Koala & execute <code className="text-white bg-[#1A1A22] px-1.5 py-0.5 rounded">./gradlew assembleDebug</code></span>
          </div>
          <span>minSdk: 26 • targetSdk: 35 • Room SQLite</span>
        </div>
      </div>
    </div>
  );
};
