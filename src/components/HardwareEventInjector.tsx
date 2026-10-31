import React from 'react';
import { Bolt, Zap, Thermometer, BatteryCharging, FastForward, CheckCircle2, RotateCcw } from 'lucide-react';
import type { PlugType, BatteryHealthState } from '../logic/calculators';

interface HardwareEventInjectorProps {
  batteryPct: number;
  setBatteryPct: (pct: number) => void;
  isCharging: boolean;
  plugType: PlugType;
  setPlugState: (isCharging: boolean, plugType: PlugType) => void;
  temperatureCelsius: number;
  setTemperatureCelsius: (t: number) => void;
  voltageMv: number;
  isReliableCapacity: boolean;
  setIsReliableCapacity: (r: boolean) => void;
  androidHealth: BatteryHealthState;
  setAndroidHealth: (h: BatteryHealthState) => void;
  onSimulateDrain: () => void;
  onSimulateChargeStep: () => void;
  onResetToDefault: () => void;
}

export const HardwareEventInjector: React.FC<HardwareEventInjectorProps> = ({
  batteryPct,
  setBatteryPct,
  isCharging,
  plugType,
  setPlugState,
  temperatureCelsius,
  setTemperatureCelsius,
  voltageMv,
  isReliableCapacity,
  setIsReliableCapacity,
  androidHealth,
  setAndroidHealth,
  onSimulateDrain,
  onSimulateChargeStep,
  onResetToDefault,
}) => {
  return (
    <div className="bg-[#121215] border border-[#232328] rounded-2xl p-4 text-xs text-[#9E9EA7]">
      <div className="flex items-center justify-between mb-3 border-b border-[#232328] pb-2">
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-[#CCFF00] animate-pulse" />
          <span className="font-bold text-[#F4F4F6] uppercase tracking-wider text-[11px]">
            Hardware Telemetry Injector (Simulator)
          </span>
        </div>
        <button
          onClick={onResetToDefault}
          className="flex items-center gap-1 hover:text-white transition-colors text-[10px] text-[#656570]"
          title="Reset telemetry"
        >
          <RotateCcw className="w-3 h-3" /> Reset
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Battery Level Slider */}
        <div>
          <div className="flex justify-between items-center mb-1">
            <span>Battery Level:</span>
            <span className="font-bold text-[#CCFF00] text-sm">{batteryPct}%</span>
          </div>
          <input
            type="range"
            min="1"
            max="100"
            value={batteryPct}
            onChange={(e) => setBatteryPct(Number(e.target.value))}
            className="w-full accent-[#CCFF00] cursor-pointer"
          />
          <div className="flex justify-between text-[10px] text-[#656570] mt-0.5">
            <span>Critical 1%</span>
            <span>Full 100%</span>
          </div>
        </div>

        {/* Charger Connection */}
        <div>
          <span className="block mb-1">Power Connection:</span>
          <div className="grid grid-cols-2 gap-1.5">
            <button
              onClick={() => setPlugState(false, 'NONE')}
              className={`py-1.5 px-2 rounded-lg font-medium text-center transition-all ${
                !isCharging
                  ? 'bg-[#232328] text-white border border-[#3E3E48]'
                  : 'bg-[#18181D] hover:bg-[#202026] text-[#656570]'
              }`}
            >
              Unplugged
            </button>
            <button
              onClick={() => setPlugState(true, 'AC')}
              className={`py-1.5 px-2 rounded-lg font-medium text-center flex items-center justify-center gap-1 transition-all ${
                isCharging && plugType === 'AC'
                  ? 'bg-[#00F0A8] text-black font-bold shadow-sm'
                  : 'bg-[#18181D] hover:bg-[#202026] text-[#00F0A8]'
              }`}
            >
              <Zap className="w-3 h-3" /> AC Fast
            </button>
            <button
              onClick={() => setPlugState(true, 'USB')}
              className={`py-1.5 px-2 rounded-lg font-medium text-center transition-all ${
                isCharging && plugType === 'USB'
                  ? 'bg-[#00F0A8] text-black font-bold shadow-sm'
                  : 'bg-[#18181D] hover:bg-[#202026] text-[#00F0A8]'
              }`}
            >
              USB 5W
            </button>
            <button
              onClick={() => setPlugState(true, 'WIRELESS')}
              className={`py-1.5 px-2 rounded-lg font-medium text-center transition-all ${
                isCharging && plugType === 'WIRELESS'
                  ? 'bg-[#00F0A8] text-black font-bold shadow-sm'
                  : 'bg-[#18181D] hover:bg-[#202026] text-[#00F0A8]'
              }`}
            >
              Wireless
            </button>
          </div>
        </div>

        {/* Thermal & Health Parameters */}
        <div>
          <div className="flex justify-between items-center mb-1">
            <span>Thermal Sensor:</span>
            <span className={`font-bold ${temperatureCelsius >= 42 ? 'text-[#FF4444]' : 'text-[#F4F4F6]'}`}>
              {temperatureCelsius.toFixed(1)}°C
            </span>
          </div>
          <input
            type="range"
            min="20"
            max="50"
            step="0.5"
            value={temperatureCelsius}
            onChange={(e) => setTemperatureCelsius(Number(e.target.value))}
            className="w-full accent-[#00F0A8] cursor-pointer"
          />
          <div className="flex items-center justify-between text-[10px] mt-1">
            <span className="text-[#656570]">Android Health:</span>
            <select
              value={androidHealth}
              onChange={(e) => setAndroidHealth(e.target.value as BatteryHealthState)}
              className="bg-[#18181D] border border-[#2D2D35] text-white rounded px-1.5 py-0.5 text-[10px]"
            >
              <option value="GOOD">GOOD</option>
              <option value="OVERHEAT">OVERHEAT</option>
              <option value="OVER_VOLTAGE">OVER_VOLTAGE</option>
              <option value="DEAD">DEAD</option>
            </select>
          </div>
        </div>

        {/* Quick Simulation Actions */}
        <div>
          <span className="block mb-1">Event Simulation:</span>
          <div className="space-y-1.5">
            <button
              onClick={onSimulateDrain}
              className="w-full py-1.5 px-2 rounded-lg bg-[#1C1C22] hover:bg-[#26262E] text-white font-medium flex items-center justify-center gap-1.5 border border-[#2D2D35]"
            >
              <FastForward className="w-3.5 h-3.5 text-[#FFB800]" />
              <span>Simulate Discharge (-5%)</span>
            </button>
            <button
              onClick={onSimulateChargeStep}
              className="w-full py-1.5 px-2 rounded-lg bg-[#1C1C22] hover:bg-[#26262E] text-white font-medium flex items-center justify-center gap-1.5 border border-[#2D2D35]"
            >
              <BatteryCharging className="w-3.5 h-3.5 text-[#00F0A8]" />
              <span>Simulate Charge Step (+8%)</span>
            </button>
            <label className="flex items-center gap-2 cursor-pointer pt-0.5">
              <input
                type="checkbox"
                checked={isReliableCapacity}
                onChange={(e) => setIsReliableCapacity(e.target.checked)}
                className="rounded accent-[#CCFF00]"
              />
              <span className="text-[11px] text-[#A0A0AA]">Hardware exposes design capacity</span>
            </label>
          </div>
        </div>
      </div>
    </div>
  );
};
