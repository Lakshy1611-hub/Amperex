import React, { useState } from 'react';
import {
  Zap,
  TrendingDown,
  Activity,
  Heart,
  Grid,
  BatteryCharging,
  Settings,
  RefreshCw,
  Clock,
  Thermometer,
  Gauge,
  ShieldAlert,
  Info,
  CheckCircle2,
  AlertTriangle,
  RotateCcw,
  Download,
  Trash2,
  Moon,
  Sun,
  Flame,
  ArrowUpRight,
  ExternalLink,
} from 'lucide-react';
import type {
  BatterySample,
  ChargingStatus,
  PlugType,
  BatteryHealthState,
  DailyBatterySummary,
  BackupPrediction,
  ChargingPrediction,
  BatteryHealthReport,
} from '../logic/calculators';

interface PhoneSimulatorProps {
  batteryPct: number;
  status: ChargingStatus;
  plugType: PlugType;
  temperatureCelsius: number;
  voltageMv: number;
  currentMa: number | null;
  samples: BatterySample[];
  dailySummary: DailyBatterySummary;
  backupPrediction: BackupPrediction;
  chargingPrediction: ChargingPrediction;
  healthReport: BatteryHealthReport;
  onRefresh: () => void;
  onClearHistory: () => void;
  onExportCsv: () => void;
  onExportJson: () => void;
}

export type TabType = 'home' | 'analytics' | 'health' | 'apps' | 'charging';

export const PhoneSimulator: React.FC<PhoneSimulatorProps> = ({
  batteryPct,
  status,
  plugType,
  temperatureCelsius,
  voltageMv,
  currentMa,
  samples,
  dailySummary,
  backupPrediction,
  chargingPrediction,
  healthReport,
  onRefresh,
  onClearHistory,
  onExportCsv,
  onExportJson,
}) => {
  const [activeTab, setActiveTab] = useState<TabType>('home');
  const scrollRef = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = 0;
    }
  }, [activeTab]);
  const [showSettings, setShowSettings] = useState(false);
  const [showClearConfirm, setShowClearConfirm] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState<'1h' | '6h' | '24h' | '7d' | '30d'>('24h');
  const [hasUsageAccess, setHasUsageAccess] = useState(true);
  const [scrubIndex, setScrubIndex] = useState<number | null>(null);
  const [useFahrenheit, setUseFahrenheit] = useState(false);
  const [amoledDark, setAmoledDark] = useState(true);

  // App Usage data
  const sampleApps = [
    { name: 'Chrome', package: 'com.android.chrome', mins: 142, last: '5m ago' },
    { name: 'YouTube', package: 'com.google.android.youtube', mins: 118, last: '24m ago' },
    { name: 'Camera', package: 'com.google.android.GoogleCamera', mins: 45, last: '1h ago' },
    { name: 'Google Maps', package: 'com.google.android.apps.maps', mins: 38, last: '2h ago' },
    { name: 'Spotify', package: 'com.spotify.music', mins: 34, last: '15m ago' },
    { name: 'WhatsApp', package: 'com.whatsapp', mins: 29, last: 'Just now' },
    { name: 'AmpereX', package: 'com.amperex.batteryintelligence', mins: 12, last: 'Active' },
  ];

  // Temp display
  const displayTemp = (celsius: number | null) => {
    if (celsius === null) return 'Unavailable';
    if (useFahrenheit) {
      return `${((celsius * 9) / 5 + 32).toFixed(1)}°F`;
    }
    return `${celsius.toFixed(1)}°C`;
  };

  const isCharging = status === 'CHARGING' || status === 'FULL';
  const ringColor = isCharging ? '#00F0A8' : batteryPct <= 15 ? '#FF4444' : batteryPct <= 25 ? '#FFB800' : '#CCFF00';

  return (
    <div className="relative mx-auto w-full max-w-[390px] h-[780px] rounded-[48px] bg-black border-[10px] border-[#1C1C22] shadow-[0_25px_60px_-15px_rgba(0,0,0,0.9)] flex flex-col overflow-hidden text-white font-sans select-none">
      {/* Phone Speaker & Dynamic Island / Camera Punch */}
      <div className="absolute top-3 left-1/2 -translate-x-1/2 w-24 h-5 bg-[#000000] rounded-full z-50 flex items-center justify-center">
        <div className="w-3 h-3 rounded-full bg-[#111115] border border-[#282830]" />
      </div>

      {/* Android Status Bar */}
      <div className="h-10 px-7 pt-2 flex items-center justify-between text-[11px] font-semibold text-[#A0A0AA] z-40 bg-black/90">
        <span>09:41</span>
        <div className="flex items-center gap-2">
          <span className="text-[10px]">5G</span>
          <div className="flex items-center gap-1">
            <span className="text-[10px]">{batteryPct}%</span>
            <div className="w-4 h-2.5 rounded-sm border border-[#A0A0AA] p-0.5 flex items-center">
              <div
                className="h-full rounded-xs transition-all duration-300"
                style={{ width: `${batteryPct}%`, backgroundColor: ringColor }}
              />
            </div>
          </div>
        </div>
      </div>

      {/* Main Screen Content Area */}
      <div ref={scrollRef} className="flex-1 overflow-y-auto px-5 py-2 scrollbar-none bg-[#000000]">
        {/* TAB 1: HOME */}
        {activeTab === 'home' && (
          <div className="space-y-4 pb-16 animate-fadeIn">
            {/* Top Bar Header */}
            <div className="flex items-center justify-between pt-1">
              <div>
                <h1 className="text-xl font-black tracking-widest text-[#CCFF00]">AMPEREX</h1>
                <p className="text-[11px] text-[#888892] uppercase tracking-wider">Battery Intelligence</p>
              </div>
              <div className="flex items-center gap-1">
                <button
                  onClick={onRefresh}
                  className="p-2 rounded-xl bg-[#141418] hover:bg-[#202028] text-[#9E9EA7] transition-colors"
                  title="Manual refresh (deduped)"
                >
                  <RefreshCw className="w-4 h-4" />
                </button>
                <button
                  onClick={() => setShowSettings(true)}
                  className="p-2 rounded-xl bg-[#141418] hover:bg-[#202028] text-[#9E9EA7] transition-colors"
                >
                  <Settings className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Circular Battery Ring Gauge */}
            <div className="flex flex-col items-center justify-center py-4">
              <div className="relative w-44 h-44 flex items-center justify-center">
                <svg className="w-full h-full -rotate-90">
                  <circle
                    cx="88"
                    cy="88"
                    r="72"
                    stroke="#1E1E24"
                    strokeWidth="12"
                    fill="transparent"
                  />
                  <circle
                    cx="88"
                    cy="88"
                    r="72"
                    stroke={ringColor}
                    strokeWidth="12"
                    strokeDasharray={452}
                    strokeDashoffset={452 - (452 * batteryPct) / 100}
                    strokeLinecap="round"
                    fill="transparent"
                    className="transition-all duration-700 ease-out"
                  />
                </svg>
                <div className="absolute flex flex-col items-center">
                  <span className="text-4xl font-black tracking-tight text-white">{batteryPct}%</span>
                  <span
                    className="text-[10px] font-bold tracking-wider uppercase mt-0.5 px-2 py-0.5 rounded-full"
                    style={{ color: ringColor, backgroundColor: `${ringColor}15` }}
                  >
                    {isCharging ? `Charging (${plugType})` : status}
                  </span>
                </div>
              </div>
            </div>

            {/* Backup Runtime Prediction Card */}
            <div className="bg-[#121215] border border-[#232328] rounded-2xl p-4">
              <div className="flex items-center gap-2 mb-1 text-[11px] font-semibold text-[#8E8E98] uppercase tracking-wider">
                <Zap className="w-3.5 h-3.5 text-[#CCFF00]" />
                <span>Estimated Remaining Runtime</span>
              </div>
              <div className="text-2xl font-black text-white">
                {backupPrediction.estimatedMinutesRemaining !== null ? (
                  <>
                    {Math.floor(backupPrediction.estimatedMinutesRemaining / 60)}h{' '}
                    {backupPrediction.estimatedMinutesRemaining % 60}m
                  </>
                ) : (
                  <span className="text-[#8E8E98] text-lg">Collecting data</span>
                )}
              </div>
              <p className="text-[11px] text-[#656572] mt-1 leading-relaxed">
                {backupPrediction.methodologyNote}
              </p>
            </div>

            {/* Hardware Telemetry 2x2 Grid */}
            <div className="grid grid-cols-2 gap-3">
              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <div className="flex items-center gap-1.5 text-[10px] text-[#8E8E98] uppercase">
                  <Thermometer className="w-3.5 h-3.5 text-[#00F0A8]" />
                  <span>Temperature</span>
                </div>
                <div className="text-lg font-bold text-white mt-1">
                  {displayTemp(temperatureCelsius)}
                </div>
                <span className="text-[10px] text-[#555562]">Battery thermal sensor</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <div className="flex items-center gap-1.5 text-[10px] text-[#8E8E98] uppercase">
                  <Gauge className="w-3.5 h-3.5 text-[#CCFF00]" />
                  <span>Voltage</span>
                </div>
                <div className="text-lg font-bold text-white mt-1">
                  {(voltageMv / 1000).toFixed(2)} V
                </div>
                <span className="text-[10px] text-[#555562]">Cell terminal voltage</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <div className="flex items-center gap-1.5 text-[10px] text-[#8E8E98] uppercase">
                  <Activity className="w-3.5 h-3.5 text-[#FFB800]" />
                  <span>Current Flow</span>
                </div>
                <div className="text-lg font-bold text-white mt-1">
                  {currentMa !== null ? `${currentMa} mA` : 'Not supported'}
                </div>
                <span className="text-[10px] text-[#555562]">
                  {isCharging ? 'Charge intake' : 'Discharge draw'}
                </span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <div className="flex items-center gap-1.5 text-[10px] text-[#8E8E98] uppercase">
                  <CheckCircle2 className="w-3.5 h-3.5 text-[#00F0A8]" />
                  <span>Reported Health</span>
                </div>
                <div className="text-lg font-bold text-white mt-1">
                  {healthReport.androidReportedHealth}
                </div>
                <span className="text-[10px] text-[#555562]">Kernel status</span>
              </div>
            </div>

            {/* Disclaimer Banner */}
            <div className="bg-[#0C0C0F] border border-[#202026] rounded-xl p-3 flex items-start gap-2 text-[11px] text-[#7A7A88]">
              <Info className="w-4 h-4 text-[#8E8E98] shrink-0 mt-0.5" />
              <span>
                AmpereX reads real Android battery hardware state. Values unsupported by the device are reported honestly rather than fabricated.
              </span>
            </div>
          </div>
        )}

        {/* TAB 2: ANALYTICS */}
        {activeTab === 'analytics' && (
          <div className="space-y-4 pb-16 animate-fadeIn">
            <div>
              <h2 className="text-xl font-bold text-white">Battery History & Timeline</h2>
              <p className="text-[11px] text-[#888892]">Real recorded observations and gross discharge analytics</p>
            </div>

            {/* Time Filter Pills */}
            <div className="flex rounded-xl bg-[#121215] border border-[#232328] p-1">
              {(['1h', '6h', '24h', '7d', '30d'] as const).map((period) => (
                <button
                  key={period}
                  onClick={() => setSelectedPeriod(period)}
                  className={`flex-1 py-1.5 text-[11px] font-bold rounded-lg transition-all ${
                    selectedPeriod === period
                      ? 'bg-[#CCFF00] text-black shadow-sm'
                      : 'text-[#8E8E98] hover:text-white'
                  }`}
                >
                  {period}
                </button>
              ))}
            </div>

            {/* Interactive Timeline Chart */}
            <div className="bg-[#121215] border border-[#232328] rounded-2xl p-4">
              <div className="flex justify-between items-center mb-3">
                <span className="text-[10px] font-bold uppercase tracking-wider text-[#888892]">
                  {scrubIndex !== null && samples[scrubIndex] ? (
                    <span className="text-[#CCFF00]">
                      {new Date(samples[scrubIndex].timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} • {samples[scrubIndex].percentage}% ({samples[scrubIndex].status})
                    </span>
                  ) : (
                    'Tap or drag chart to scrub'
                  )}
                </span>
                <span className="text-[10px] text-[#555562]">{samples.length} records</span>
              </div>

              {/* Chart SVG */}
              <div className="relative h-40 w-full">
                <svg viewBox="0 0 320 140" className="w-full h-full overflow-visible">
                  {/* Grid Lines */}
                  {[0, 25, 50, 75, 100].map((level) => (
                    <line
                      key={level}
                      x1="0"
                      y1={130 - (level / 100) * 115}
                      x2="320"
                      y2={130 - (level / 100) * 115}
                      stroke="#1F1F24"
                      strokeWidth="1"
                    />
                  ))}

                  {/* Battery Line Path */}
                  {samples.length > 1 && (
                    <polyline
                      fill="none"
                      stroke="#CCFF00"
                      strokeWidth="2.5"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      points={samples
                        .map((s, i) => {
                          const x = (i / (samples.length - 1)) * 320;
                          const y = 130 - (s.percentage / 100) * 115;
                          return `${x},${y}`;
                        })
                        .join(' ')}
                    />
                  )}

                  {/* Observation nodes */}
                  {samples.map((s, i) => {
                    const x = (i / (samples.length - 1)) * 320;
                    const y = 130 - (s.percentage / 100) * 115;
                    const isChg = s.status === 'CHARGING';
                    return (
                      <circle
                        key={s.id}
                        cx={x}
                        cy={y}
                        r={scrubIndex === i ? 6 : 3.5}
                        fill={isChg ? '#00F0A8' : '#CCFF00'}
                        className="cursor-pointer"
                        onMouseEnter={() => setScrubIndex(i)}
                        onClick={() => setScrubIndex(i)}
                      />
                    );
                  })}
                </svg>
              </div>
            </div>

            {/* Daily Battery Report Section */}
            <div className="flex items-center justify-between pt-1">
              <h3 className="text-sm font-bold text-white uppercase tracking-wider">Today's Daily Report</h3>
              {dailySummary.isPartialData && (
                <span className="text-[9px] font-bold px-2 py-0.5 rounded bg-[#2A2315] text-[#FFB800] border border-[#3E341C]">
                  PARTIAL COVERAGE
                </span>
              )}
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Gross Discharge</span>
                <div className="text-xl font-black text-[#FF4444] mt-0.5">
                  -{dailySummary.grossDischarge}%
                </div>
                <span className="text-[10px] text-[#555562]">Sum of all battery drops</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Charging Gain</span>
                <div className="text-xl font-black text-[#00F0A8] mt-0.5">
                  +{dailySummary.grossChargingGain}%
                </div>
                <span className="text-[10px] text-[#555562]">Sum of charger boosts</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Net Change</span>
                <div className="text-xl font-black text-white mt-0.5">
                  {dailySummary.netChange > 0 ? `+${dailySummary.netChange}%` : `${dailySummary.netChange}%`}
                </div>
                <span className="text-[10px] text-[#555562]">Ending minus Starting %</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Charging Sessions</span>
                <div className="text-xl font-black text-[#CCFF00] mt-0.5">
                  {dailySummary.chargingSessionCount}
                </div>
                <span className="text-[10px] text-[#555562]">Plug events today</span>
              </div>
            </div>

            {/* Calculation Guarantee Note */}
            <div className="bg-[#0C0C0F] border border-[#202026] rounded-xl p-3 text-[11px] text-[#7A7A88] leading-relaxed">
              <p>
                <strong>Calculation Fidelity:</strong> Gross discharge is never calculated merely by subtracting ending % from starting %. For instance, if your phone drops from 80% to 40% (-40%), charges to 70% (+30%), and drops to 50% (-20%), AmpereX records 60% gross discharge and 30% gain.
              </p>
            </div>
          </div>
        )}

        {/* TAB 3: HEALTH */}
        {activeTab === 'health' && (
          <div className="space-y-4 pb-16 animate-fadeIn">
            <div>
              <h2 className="text-xl font-bold text-white">Battery Health & Capacity</h2>
              <p className="text-[11px] text-[#888892]">Degradation diagnostics and hardware capacity</p>
            </div>

            {/* Capacity Retention Card */}
            <div className="bg-[#121215] border border-[#232328] rounded-2xl p-4">
              <div className="flex justify-between items-center mb-2">
                <span className="text-[10px] font-bold uppercase tracking-wider text-[#888892]">
                  Capacity Retention
                </span>
                <span
                  className={`text-[9px] font-bold px-2 py-0.5 rounded ${
                    healthReport.isReliableCapacityAvailable
                      ? 'bg-[#152815] text-[#CCFF00] border border-[#264426]'
                      : 'bg-[#25252A] text-[#8E8E98]'
                  }`}
                >
                  {healthReport.isReliableCapacityAvailable ? 'HARDWARE VERIFIED' : 'UNAVAILABLE'}
                </span>
              </div>

              {healthReport.capacityHealthPercentage !== null ? (
                <div className="text-4xl font-black text-[#CCFF00] my-1">
                  {healthReport.capacityHealthPercentage.toFixed(1)}%
                </div>
              ) : (
                <div className="space-y-1 my-2">
                  <div className="text-xl font-bold text-[#A0A0AA]">Unavailable</div>
                  <div className="text-[12px] text-[#FFB800] leading-snug">
                    Battery capacity health cannot be measured reliably on this device.
                  </div>
                </div>
              )}

              <p className="text-[11px] text-[#656572] leading-relaxed mt-2">
                {healthReport.explanation}
              </p>
            </div>

            {/* Hardware Values Grid */}
            <div className="grid grid-cols-2 gap-3">
              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Reported Health</span>
                <div className="text-lg font-bold text-[#00F0A8] mt-1">
                  {healthReport.androidReportedHealth}
                </div>
                <span className="text-[10px] text-[#555562]">BatteryManager status</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Cycle Count</span>
                <div className="text-lg font-bold text-white mt-1">
                  {healthReport.cycleCount !== null ? `${healthReport.cycleCount} cycles` : 'Not exposed'}
                </div>
                <span className="text-[10px] text-[#555562]">Hardware cycle counter</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Full Charge Cap.</span>
                <div className="text-lg font-bold text-white mt-1">
                  {healthReport.measuredFullCapacityMah ? `${healthReport.measuredFullCapacityMah} mAh` : 'Unavailable'}
                </div>
                <span className="text-[10px] text-[#555562]">Current max Coulomb</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Factory Design</span>
                <div className="text-lg font-bold text-white mt-1">
                  {healthReport.designCapacityMah ? `${healthReport.designCapacityMah} mAh` : 'Unavailable'}
                </div>
                <span className="text-[10px] text-[#555562]">Factory nominal spec</span>
              </div>
            </div>

            <div className="bg-[#0C0C0F] border border-[#202026] rounded-xl p-3 text-[11px] text-[#7A7A88] leading-relaxed">
              <strong>Honesty Principle:</strong> Android's generic "Good" status is not proof of 100% capacity. Standard Android public APIs do not expose verified factory design capacity on every device model. AmpereX refuses to invent a fake 99% or 100% score.
            </div>
          </div>
        )}

        {/* TAB 4: APPS */}
        {activeTab === 'apps' && (
          <div className="space-y-4 pb-16 animate-fadeIn">
            <div>
              <h2 className="text-xl font-bold text-white">App Foreground Usage</h2>
              <p className="text-[11px] text-[#888892]">Actual active usage time via Android UsageStatsManager</p>
            </div>

            {!hasUsageAccess ? (
              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-5 text-center">
                <ShieldAlert className="w-10 h-10 text-[#CCFF00] mx-auto mb-2" />
                <h3 className="text-base font-bold text-white">Usage Access Permission Required</h3>
                <p className="text-xs text-[#8E8E98] mt-1 mb-4 leading-relaxed">
                  Android requires explicit permission under Special App Access to inspect which apps you use. All data stays strictly on your phone.
                </p>
                <button
                  onClick={() => setHasUsageAccess(true)}
                  className="w-full py-2.5 rounded-xl bg-[#CCFF00] text-black font-bold text-xs"
                >
                  Grant Usage Access (Simulate)
                </button>
              </div>
            ) : (
              <>
                {/* Important Disclaimer Banner */}
                <div className="bg-[#0C0C0F] border border-[#202026] rounded-xl p-3 flex items-start gap-2 text-[11px] text-[#7A7A88]">
                  <AlertTriangle className="w-4 h-4 text-[#FFB800] shrink-0 mt-0.5" />
                  <span>
                    <strong>Notice:</strong> Foreground usage duration is NOT battery consumption. Android public APIs do not expose exact per-app mAh drain. We display actual active time without fake percentages.
                  </span>
                </div>

                <div className="space-y-2">
                  {sampleApps.map((app, idx) => (
                    <div
                      key={app.package}
                      className="bg-[#121215] border border-[#232328] rounded-xl p-3 flex items-center justify-between"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-7 h-7 rounded-full bg-[#1E1E24] flex items-center justify-center text-xs font-bold text-[#CCFF00]">
                          {idx + 1}
                        </div>
                        <div>
                          <div className="text-sm font-semibold text-white">{app.name}</div>
                          <div className="text-[10px] text-[#555562]">{app.package}</div>
                        </div>
                      </div>

                      <div className="text-right">
                        <div className="text-sm font-bold text-white">
                          {Math.floor(app.mins / 60) > 0 ? `${Math.floor(app.mins / 60)}h ` : ''}
                          {app.mins % 60}m
                        </div>
                        <div className="text-[10px] text-[#8E8E98]">Foreground Active</div>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>
        )}

        {/* TAB 5: CHARGING */}
        {activeTab === 'charging' && (
          <div className="space-y-4 pb-16 animate-fadeIn">
            <div>
              <h2 className="text-xl font-bold text-white">Charging Analytics</h2>
              <p className="text-[11px] text-[#888892]">Real-time intake rate, session duration, and completion predictions</p>
            </div>

            {/* Active Session Status */}
            <div className="bg-[#121215] border border-[#232328] rounded-2xl p-4">
              <div className="flex justify-between items-center mb-2">
                <span className="text-[10px] font-bold uppercase tracking-wider text-[#00F0A8] flex items-center gap-1.5">
                  <BatteryCharging className="w-3.5 h-3.5" />
                  {isCharging ? 'ACTIVE CHARGING SESSION' : 'UNPLUGGED / STANDBY'}
                </span>
                <span className="text-[10px] text-[#888892]">
                  {plugType !== 'NONE' ? plugType : 'Battery'}
                </span>
              </div>

              <div className="flex items-baseline justify-between my-2">
                <div>
                  <div className="text-4xl font-black text-white">{batteryPct}%</div>
                  <div className="text-[11px] text-[#8E8E98]">Current Level</div>
                </div>
                {isCharging && (
                  <div className="text-right">
                    <div className="text-xl font-bold text-[#00F0A8]">+18%</div>
                    <div className="text-[11px] text-[#555562]">Gained since 62%</div>
                  </div>
                )}
              </div>

              <p className="text-[11px] text-[#656572] leading-relaxed mt-2 border-t border-[#1C1C22] pt-2">
                {chargingPrediction.explanation}
              </p>
            </div>

            {/* Time to 80% & Time to 100% */}
            <div className="grid grid-cols-2 gap-3">
              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Est. Time to 80%</span>
                <div className="text-lg font-bold text-[#00F0A8] mt-1">
                  {chargingPrediction.estimatedMinutesTo80 !== null
                    ? `${chargingPrediction.estimatedMinutesTo80} min`
                    : isCharging
                    ? 'Measuring...'
                    : 'Not charging'}
                </div>
                <span className="text-[10px] text-[#555562]">Recommended Li-ion limit</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Est. Time to Full</span>
                <div className="text-lg font-bold text-[#CCFF00] mt-1">
                  {chargingPrediction.estimatedMinutesToFull !== null
                    ? `${chargingPrediction.estimatedMinutesToFull} min`
                    : isCharging
                    ? 'Measuring...'
                    : 'Not charging'}
                </div>
                <span className="text-[10px] text-[#555562]">Includes CV taper above 80%</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Observed Charge Rate</span>
                <div className="text-lg font-bold text-white mt-1">
                  {chargingPrediction.observedRatePerHour > 0
                    ? `${chargingPrediction.observedRatePerHour}%/hr`
                    : 'N/A'}
                </div>
                <span className="text-[10px] text-[#555562]">Recent intake velocity</span>
              </div>

              <div className="bg-[#121215] border border-[#232328] rounded-2xl p-3.5">
                <span className="text-[10px] text-[#888892] uppercase font-semibold">Current Flow</span>
                <div className="text-lg font-bold text-white mt-1">
                  {currentMa ? `${currentMa} mA` : 'Not supported'}
                </div>
                <span className="text-[10px] text-[#555562]">Hardware intake current</span>
              </div>
            </div>

            {/* Recent Sessions */}
            <h3 className="text-sm font-bold text-white uppercase tracking-wider pt-1">Recent Charging Sessions</h3>
            <div className="space-y-2">
              {[
                { time: 'Today, 08:15', plug: 'AC Fast Charger', duration: '34 min', gain: '+42%', range: '38% → 80%' },
                { time: 'Yesterday, 22:30', plug: 'Wireless Qi', duration: '1h 15m', gain: '+28%', range: '52% → 80%' },
                { time: 'Oct 29, 14:10', plug: 'USB Port', duration: '45 min', gain: '+15%', range: '45% → 60%' },
              ].map((sess, idx) => (
                <div
                  key={idx}
                  className="bg-[#121215] border border-[#232328] rounded-xl p-3 flex items-center justify-between"
                >
                  <div>
                    <div className="text-xs font-semibold text-white">{sess.time}</div>
                    <div className="text-[10px] text-[#555562]">{sess.plug} • {sess.duration}</div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-bold text-[#00F0A8]">{sess.gain}</div>
                    <div className="text-[10px] text-[#8E8E98]">{sess.range}</div>
                  </div>
                </div>
              ))}
            </div>

            <div className="bg-[#0C0C0F] border border-[#202026] rounded-xl p-3 text-[11px] text-[#7A7A88]">
              <strong>Cycle Note:</strong> Partial charging sessions do not count as a full cycle. A battery cycle is defined by 100% cumulative discharge and replenishment.
            </div>
          </div>
        )}
      </div>

      {/* Settings Modal (Overlay inside phone) */}
      {showSettings && (
        <div className="absolute inset-0 bg-black/95 z-50 p-6 flex flex-col justify-between overflow-y-auto animate-fadeIn">
          <div className="space-y-5">
            <div className="flex items-center justify-between border-b border-[#232328] pb-3">
              <h2 className="text-lg font-bold text-white">Settings & Privacy</h2>
              <button
                onClick={() => setShowSettings(false)}
                className="text-xs text-[#CCFF00] font-bold"
              >
                Done
              </button>
            </div>

            {/* Appearance Controls */}
            <div className="space-y-3">
              <span className="text-[10px] font-bold uppercase tracking-wider text-[#888892]">Preferences</span>
              <div className="bg-[#121215] border border-[#232328] rounded-xl p-3 space-y-3">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-xs font-semibold text-white">AMOLED Pure Black</div>
                    <div className="text-[10px] text-[#555562]">Deep black pixels save screen power</div>
                  </div>
                  <input
                    type="checkbox"
                    checked={amoledDark}
                    onChange={(e) => setAmoledDark(e.target.checked)}
                    className="accent-[#CCFF00] w-4 h-4 cursor-pointer"
                  />
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-[#1C1C22]">
                  <div>
                    <div className="text-xs font-semibold text-white">Fahrenheit Temperature</div>
                    <div className="text-[10px] text-[#555562]">Display degrees in °F instead of °C</div>
                  </div>
                  <input
                    type="checkbox"
                    checked={useFahrenheit}
                    onChange={(e) => setUseFahrenheit(e.target.checked)}
                    className="accent-[#CCFF00] w-4 h-4 cursor-pointer"
                  />
                </div>
              </div>
            </div>

            {/* Export Records */}
            <div className="space-y-2">
              <span className="text-[10px] font-bold uppercase tracking-wider text-[#888892]">Data Portability</span>
              <div className="bg-[#121215] border border-[#232328] rounded-xl p-3">
                <p className="text-[11px] text-[#8E8E98] mb-3 leading-relaxed">
                  Export timestamped battery percentage, voltage, and charging logs to your device.
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={onExportCsv}
                    className="flex-1 py-2 rounded-lg bg-[#CCFF00] text-black font-bold text-xs flex items-center justify-center gap-1.5"
                  >
                    <Download className="w-3.5 h-3.5" /> CSV
                  </button>
                  <button
                    onClick={onExportJson}
                    className="flex-1 py-2 rounded-lg bg-[#222228] hover:bg-[#2A2A32] text-white font-bold text-xs"
                  >
                    JSON
                  </button>
                </div>
              </div>
            </div>

            {/* Privacy Architecture */}
            <div className="space-y-2">
              <span className="text-[10px] font-bold uppercase tracking-wider text-[#888892]">Privacy Architecture</span>
              <div className="bg-[#121215] border border-[#232328] rounded-xl p-3 text-[11px] text-[#8E8E98] space-y-2 leading-relaxed">
                <p>• <strong>100% Offline:</strong> No cloud telemetry, analytics SDKs, or remote servers.</p>
                <p>• <strong>Android Doze Friendly:</strong> Uses system broadcasts rather than keeping permanent CPU wake-locks.</p>
                <p>• <strong>No Accessibility Abuse:</strong> Never asks for Accessibility permissions.</p>
              </div>
            </div>

            {/* Danger Zone: Clear History */}
            <div className="pt-2">
              <button
                onClick={() => setShowClearConfirm(true)}
                className="w-full py-2.5 rounded-xl border border-[#FF4444]/40 text-[#FF4444] text-xs font-semibold flex items-center justify-center gap-2"
              >
                <Trash2 className="w-3.5 h-3.5" /> Clear All Local Battery History
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Clear Confirmation Dialog */}
      {showClearConfirm && (
        <div className="absolute inset-0 bg-black/90 z-50 flex items-center justify-center p-6 animate-fadeIn">
          <div className="bg-[#18181D] border border-[#2C2C35] rounded-2xl p-5 max-w-xs text-center space-y-3">
            <h3 className="text-base font-bold text-white">Clear All Battery Data?</h3>
            <p className="text-xs text-[#8E8E98] leading-relaxed">
              This will erase all recorded Room database samples, charging sessions, and daily reports from local device storage.
            </p>
            <div className="flex gap-2 pt-2">
              <button
                onClick={() => setShowClearConfirm(false)}
                className="flex-1 py-2 rounded-xl bg-[#25252D] text-[#8E8E98] text-xs font-semibold"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  onClearHistory();
                  setShowClearConfirm(false);
                }}
                className="flex-1 py-2 rounded-xl bg-[#FF4444] text-white text-xs font-bold"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Android Bottom Navigation Bar */}
      <div className="h-16 bg-[#121215] border-t border-[#202026] flex items-center justify-around px-2 z-40">
        {[
          { id: 'home', label: 'Home', icon: Zap },
          { id: 'analytics', label: 'Analytics', icon: TrendingDown },
          { id: 'health', label: 'Health', icon: Heart },
          { id: 'apps', label: 'Apps', icon: Grid },
          { id: 'charging', label: 'Charging', icon: BatteryCharging },
        ].map((tab) => {
          const IconComp = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as TabType)}
              className="flex flex-col items-center justify-center py-1 px-3 transition-colors group"
            >
              <IconComp
                className={`w-5 h-5 transition-transform ${
                  isActive ? 'text-[#CCFF00] scale-110' : 'text-[#656570] group-hover:text-white'
                }`}
              />
              <span
                className={`text-[10px] mt-1 font-medium ${
                  isActive ? 'text-[#CCFF00] font-bold' : 'text-[#656570]'
                }`}
              >
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
};
