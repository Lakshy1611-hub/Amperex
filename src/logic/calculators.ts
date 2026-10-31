/**
 * Pure calculation logic for AmpereX Battery Intelligence.
 * Decoupled from UI for deterministic testing and runtime simulation.
 */

export type ChargingStatus = 'CHARGING' | 'DISCHARGING' | 'FULL' | 'NOT_CHARGING' | 'UNKNOWN';
export type PlugType = 'NONE' | 'AC' | 'USB' | 'WIRELESS' | 'DOCK' | 'UNKNOWN';
export type BatteryHealthState = 'GOOD' | 'OVERHEAT' | 'DEAD' | 'OVER_VOLTAGE' | 'UNSPECIFIED_FAILURE' | 'COLD' | 'UNKNOWN';
export type EstimationConfidence = 'HIGH' | 'MEDIUM' | 'LOW' | 'INSUFFICIENT_DATA';

export interface BatterySample {
  id: number;
  timestamp: number;
  percentage: number;
  status: ChargingStatus;
  plugType: PlugType;
  temperatureCelsius: number | null;
  voltageMv: number | null;
  currentMa: number | null;
  chargeCounterUah?: number | null;
}

export interface DailyBatterySummary {
  dateEpochDay: number;
  startingPercentage: number | null;
  endingPercentage: number | null;
  grossDischarge: number;
  grossChargingGain: number;
  netChange: number;
  trackedScreenOnMinutes: number | null;
  chargingSessionCount: number;
  averageDischargeRatePerHour: number | null;
  peakTemperatureCelsius: number | null;
  isPartialData: boolean;
  totalSamples: number;
}

export interface BatteryHealthReport {
  androidReportedHealth: BatteryHealthState;
  designCapacityMah: number | null;
  measuredFullCapacityMah: number | null;
  capacityHealthPercentage: number | null;
  cycleCount: number | null;
  isReliableCapacityAvailable: boolean;
  historicalReadingsCount: number;
  explanation: string;
}

export interface BackupPrediction {
  estimatedMinutesRemaining: number | null;
  confidence: EstimationConfidence;
  dischargeRatePerHour: number;
  recentSampleCount: number;
  methodologyNote: string;
}

export interface ChargingPrediction {
  estimatedMinutesTo80: number | null;
  estimatedMinutesToFull: number | null;
  observedRatePerHour: number;
  isEstimated: boolean;
  explanation: string;
}

/**
 * Calculates smoothed discharge rate (%/hr) from observed drop steps
 */
export function calculateDischargeRatePerHour(samples: BatterySample[]): number {
  const discharging = samples
    .filter((s) => s.status === 'DISCHARGING')
    .sort((a, b) => a.timestamp - b.timestamp);

  if (discharging.length < 2) return 0;

  const rates: number[] = [];
  for (let i = 1; i < discharging.length; i++) {
    const prev = discharging[i - 1];
    const curr = discharging[i];
    const deltaHours = (curr.timestamp - prev.timestamp) / 3_600_000;
    const drop = prev.percentage - curr.percentage;

    if (deltaHours >= 0.003 && deltaHours <= 12 && drop >= 0) {
      const rate = drop / deltaHours;
      if (rate >= 0.1 && rate <= 60) {
        rates.push(rate);
      }
    }
  }

  if (rates.length === 0) return 0;

  let weightedSum = 0;
  let weightTotal = 0;
  rates.forEach((rate, idx) => {
    const weight = 1.0 + idx / rates.length;
    weightedSum += rate * weight;
    weightTotal += weight;
  });

  return weightTotal > 0 ? weightedSum / weightTotal : 0;
}

/**
 * Accurately calculates gross discharge, charging gain, and net change.
 * Gross discharge is never calculated solely as (ending - starting).
 */
export function computeDailySummary(
  dateEpochDay: number,
  daySamples: BatterySample[],
  trackedScreenOnMinutes: number | null = null
): DailyBatterySummary {
  if (daySamples.length === 0) {
    return {
      dateEpochDay,
      startingPercentage: null,
      endingPercentage: null,
      grossDischarge: 0,
      grossChargingGain: 0,
      netChange: 0,
      trackedScreenOnMinutes,
      chargingSessionCount: 0,
      averageDischargeRatePerHour: null,
      peakTemperatureCelsius: null,
      isPartialData: true,
      totalSamples: 0,
    };
  }

  const sorted = [...daySamples].sort((a, b) => a.timestamp - b.timestamp);
  const startingPct = sorted[0].percentage;
  const endingPct = sorted[sorted.length - 1].percentage;

  let grossDischarge = 0;
  let grossChargingGain = 0;
  let chargingSessions = 0;
  let inChargingSession = false;
  let peakTemp: number | null = null;
  const dischargeDropsWithTime: { drop: number; hours: number }[] = [];

  for (let i = 0; i < sorted.length; i++) {
    const curr = sorted[i];

    if (curr.temperatureCelsius !== null) {
      peakTemp = peakTemp === null ? curr.temperatureCelsius : Math.max(peakTemp, curr.temperatureCelsius);
    }

    const isChargingNow = curr.status === 'CHARGING' || curr.status === 'FULL';
    if (isChargingNow && !inChargingSession) {
      chargingSessions++;
      inChargingSession = true;
    } else if (!isChargingNow && inChargingSession) {
      inChargingSession = false;
    }

    if (i > 0) {
      const prev = sorted[i - 1];
      const delta = curr.percentage - prev.percentage;
      const deltaHours = (curr.timestamp - prev.timestamp) / 3_600_000;

      if (delta < 0) {
        const drop = -delta;
        grossDischarge += drop;
        if (deltaHours >= 0.003 && deltaHours <= 12) {
          dischargeDropsWithTime.push({ drop, hours: deltaHours });
        }
      } else if (delta > 0) {
        grossChargingGain += delta;
      }
    }
  }

  const netChange = endingPct - startingPct;

  let avgDischargeRate: number | null = null;
  if (dischargeDropsWithTime.length > 0) {
    const totalDrop = dischargeDropsWithTime.reduce((sum, d) => sum + d.drop, 0);
    const totalHours = dischargeDropsWithTime.reduce((sum, d) => sum + d.hours, 0);
    if (totalHours > 0.05) {
      avgDischargeRate = totalDrop / totalHours;
    }
  }

  const firstTs = sorted[0].timestamp;
  const lastTs = sorted[sorted.length - 1].timestamp;
  const coverageHours = (lastTs - firstTs) / 3_600_000;
  const isPartial = coverageHours < 18.0 || sorted.length < 12;

  return {
    dateEpochDay,
    startingPercentage: startingPct,
    endingPercentage: endingPct,
    grossDischarge,
    grossChargingGain,
    netChange,
    trackedScreenOnMinutes,
    chargingSessionCount: chargingSessions,
    averageDischargeRatePerHour: avgDischargeRate !== null ? Math.round(avgDischargeRate * 10) / 10 : null,
    peakTemperatureCelsius: peakTemp,
    isPartialData: isPartial,
    totalSamples: sorted.length,
  };
}

/**
 * Honest Battery Health calculation
 */
export function evaluateBatteryHealth(
  androidReportedHealth: BatteryHealthState,
  designCapacityMah: number | null,
  measuredFullCapacityMah: number | null,
  cycleCount: number | null,
  historicalObservationsCount: number
): BatteryHealthReport {
  const isValidDesign = designCapacityMah !== null && designCapacityMah >= 500 && designCapacityMah <= 15000;
  const isValidMeasured = measuredFullCapacityMah !== null && measuredFullCapacityMah >= 400 && measuredFullCapacityMah <= 15000;
  const isReliable = isValidDesign && isValidMeasured;

  let capacityHealthPercentage: number | null = null;
  if (isReliable) {
    const ratio = (measuredFullCapacityMah! / designCapacityMah!) * 100;
    if (ratio >= 30 && ratio <= 115) {
      capacityHealthPercentage = Math.round(ratio * 10) / 10;
    }
  }

  const explanation = capacityHealthPercentage !== null
    ? `Measured via hardware full-charge capacity (${measuredFullCapacityMah} mAh) relative to factory design capacity (${designCapacityMah} mAh). Android reported status: ${androidReportedHealth}.`
    : !isReliable
    ? `Battery capacity health cannot be measured reliably on this device. Standard Android public APIs on this hardware do not expose verified factory design capacity or full-charge Coulomb counters. System reports status as ${androidReportedHealth}.`
    : 'Capacity readings from system counters were out of expected operational bounds and have been excluded to prevent inaccurate reporting.';

  return {
    androidReportedHealth,
    designCapacityMah: isValidDesign ? designCapacityMah : null,
    measuredFullCapacityMah: isValidMeasured ? measuredFullCapacityMah : null,
    capacityHealthPercentage,
    cycleCount: cycleCount !== null && cycleCount >= 0 && cycleCount <= 10000 ? cycleCount : null,
    isReliableCapacityAvailable: capacityHealthPercentage !== null,
    historicalReadingsCount: historicalObservationsCount,
    explanation,
  };
}

/**
 * Predicts battery runtime in minutes based on real recent discharge rate
 */
export function predictRemainingRuntime(
  currentPercentage: number,
  isCharging: boolean,
  recentSamples: BatterySample[]
): BackupPrediction {
  if (isCharging) {
    return {
      estimatedMinutesRemaining: null,
      confidence: 'INSUFFICIENT_DATA',
      dischargeRatePerHour: 0,
      recentSampleCount: recentSamples.length,
      methodologyNote: 'Device is currently charging. Runtime on battery prediction pauses during power input.',
    };
  }

  if (currentPercentage <= 0) {
    return {
      estimatedMinutesRemaining: 0,
      confidence: 'HIGH',
      dischargeRatePerHour: 0,
      recentSampleCount: recentSamples.length,
      methodologyNote: 'Battery level is depleted.',
    };
  }

  const discharging = recentSamples
    .filter((s) => s.status === 'DISCHARGING')
    .sort((a, b) => a.timestamp - b.timestamp);

  if (discharging.length < 2) {
    return {
      estimatedMinutesRemaining: null,
      confidence: 'INSUFFICIENT_DATA',
      dischargeRatePerHour: 0,
      recentSampleCount: discharging.length,
      methodologyNote: 'Collecting data... At least two recorded battery drop events are required for honest estimation.',
    };
  }

  const first = discharging[0];
  const last = discharging[discharging.length - 1];
  const durationSpanHours = (last.timestamp - first.timestamp) / 3_600_000;
  const smoothedRate = calculateDischargeRatePerHour(discharging);

  if (smoothedRate <= 0.1 || durationSpanHours < 0.05) {
    return {
      estimatedMinutesRemaining: null,
      confidence: 'INSUFFICIENT_DATA',
      dischargeRatePerHour: smoothedRate,
      recentSampleCount: discharging.length,
      methodologyNote: `Collecting data... Observed discharge duration (${Math.round(durationSpanHours * 60)} min) is currently too short for stable prediction.`,
    };
  }

  const hoursRemaining = currentPercentage / smoothedRate;
  const minutesRemaining = Math.min(Math.max(Math.round(hoursRemaining * 60), 1), 7200);

  let confidence: EstimationConfidence = 'LOW';
  if (discharging.length >= 10 && durationSpanHours >= 2.0) {
    confidence = 'HIGH';
  } else if (discharging.length >= 4 && durationSpanHours >= 0.5) {
    confidence = 'MEDIUM';
  }

  const note = `Estimated based on observed discharge of ${smoothedRate.toFixed(1)}%/hr across ${discharging.length} observations (${confidence} confidence). Actual runtime varies with screen brightness, network signals, and active workloads.`;

  return {
    estimatedMinutesRemaining: minutesRemaining,
    confidence,
    dischargeRatePerHour: Math.round(smoothedRate * 10) / 10,
    recentSampleCount: discharging.length,
    methodologyNote: note,
  };
}

/**
 * Charging progress prediction with Li-ion constant-voltage taper model above 80%
 */
export function calculateChargingPrediction(
  currentPercentage: number,
  isCharging: boolean,
  sessionSamples: BatterySample[]
): ChargingPrediction {
  if (!isCharging || currentPercentage >= 100) {
    return {
      estimatedMinutesTo80: currentPercentage >= 80 ? 0 : null,
      estimatedMinutesToFull: currentPercentage >= 100 ? 0 : null,
      observedRatePerHour: 0,
      isEstimated: false,
      explanation: currentPercentage >= 100 ? 'Battery is fully charged.' : 'Device is not currently connected to a charger.',
    };
  }

  const chargingSamples = sessionSamples
    .filter((s) => s.status === 'CHARGING')
    .sort((a, b) => a.timestamp - b.timestamp);

  if (chargingSamples.length < 2) {
    return {
      estimatedMinutesTo80: null,
      estimatedMinutesToFull: null,
      observedRatePerHour: 0,
      isEstimated: true,
      explanation: 'Measuring charging current and initial progress... Please wait 2-3 minutes for rate stabilization.',
    };
  }

  const first = chargingSamples[0];
  const last = chargingSamples[chargingSamples.length - 1];
  const deltaHours = (last.timestamp - first.timestamp) / 3_600_000;
  const deltaPct = last.percentage - first.percentage;

  let observedRate = 0;
  if (deltaHours > 0.01 && deltaPct > 0) {
    observedRate = Math.min(Math.max(deltaPct / deltaHours, 2), 150);
  } else if (last.currentMa && last.currentMa > 100) {
    observedRate = Math.min(Math.max((last.currentMa / 4500) * 100, 2), 120);
  }

  if (observedRate < 1) {
    return {
      estimatedMinutesTo80: null,
      estimatedMinutesToFull: null,
      observedRatePerHour: 0,
      isEstimated: true,
      explanation: 'Charging speed is slow or still stabilizing. Insufficient gain observed to calculate ETA.',
    };
  }

  // Time to 80%
  let minTo80: number | null = null;
  if (currentPercentage < 80) {
    const remainingTo80 = 80 - currentPercentage;
    minTo80 = Math.max(Math.round((remainingTo80 / observedRate) * 60), 1);
  } else {
    minTo80 = 0;
  }

  // Time to 100% with taper
  let minToFull = 0;
  if (currentPercentage < 80) {
    const hoursUnder80 = (80 - currentPercentage) / observedRate;
    const hoursOver80 = 20 / (observedRate * 0.70);
    minToFull = Math.max(Math.round((hoursUnder80 + hoursOver80) * 60), 1);
  } else {
    const remainingPct = 100 - currentPercentage;
    const taperedRate = observedRate * 0.70;
    minToFull = Math.max(Math.round((remainingPct / taperedRate) * 60), 1);
  }

  const explanation = `Estimated based on observed charging rate of ${observedRate.toFixed(1)}%/hr across ${chargingSamples.length} points. Accounts for standard Li-ion constant-voltage taper above 80%.`;

  return {
    estimatedMinutesTo80: minTo80,
    estimatedMinutesToFull: minToFull,
    observedRatePerHour: Math.round(observedRate * 10) / 10,
    isEstimated: true,
    explanation,
  };
}

/**
 * Filter duplicates within a timestamp window
 */
export function filterDuplicateSamples(newSample: BatterySample, existing: BatterySample[]): boolean {
  if (existing.length === 0) return false;
  const latest = existing[existing.length - 1];
  const sameValues =
    latest.percentage === newSample.percentage &&
    latest.status === newSample.status &&
    latest.plugType === newSample.plugType;
  const withinWindow = Math.abs(newSample.timestamp - latest.timestamp) < 60_000;
  return sameValues && withinWindow;
}
