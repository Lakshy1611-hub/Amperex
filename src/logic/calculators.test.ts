import test from 'node:test';
import assert from 'node:assert/strict';
import {
  calculateDischargeRatePerHour,
  computeDailySummary,
  evaluateBatteryHealth,
  predictRemainingRuntime,
  calculateChargingPrediction,
  filterDuplicateSamples,
  type BatterySample,
} from './calculators';

test('Gross discharge vs charging gain separation', () => {
  // Scenario: Starts at 80% -> drops to 40% (40 drop), charges to 70% (30 gain), drops to 50% (20 drop)
  // Gross discharge = 40 + 20 = 60
  // Gross charging gain = 30
  // Net change = 50 - 80 = -30
  const baseTime = 1700000000000;
  const samples: BatterySample[] = [
    { id: 1, timestamp: baseTime + 0, percentage: 80, status: 'DISCHARGING', plugType: 'NONE', temperatureCelsius: 28, voltageMv: 4100, currentMa: -350 },
    { id: 2, timestamp: baseTime + 3600_000 * 4, percentage: 40, status: 'DISCHARGING', plugType: 'NONE', temperatureCelsius: 29, voltageMv: 3750, currentMa: -350 },
    { id: 3, timestamp: baseTime + 3600_000 * 6, percentage: 70, status: 'CHARGING', plugType: 'AC', temperatureCelsius: 32, voltageMv: 4250, currentMa: 1800 },
    { id: 4, timestamp: baseTime + 3600_000 * 8, percentage: 50, status: 'DISCHARGING', plugType: 'NONE', temperatureCelsius: 27, voltageMv: 3850, currentMa: -320 },
  ];

  const summary = computeDailySummary(19680, samples);
  assert.equal(summary.grossDischarge, 60, 'Gross discharge must be 60%');
  assert.equal(summary.grossChargingGain, 30, 'Gross charging gain must be 30%');
  assert.equal(summary.netChange, -30, 'Net change must be -30%');
  assert.equal(summary.startingPercentage, 80);
  assert.equal(summary.endingPercentage, 50);
  assert.equal(summary.chargingSessionCount, 1);
});

test('Battery health honesty & boundary validation', () => {
  // Valid measurement: 4500 design, 4050 measured => 90.0%
  const valid = evaluateBatteryHealth('GOOD', 4500, 4050, 150, 20);
  assert.equal(valid.isReliableCapacityAvailable, true);
  assert.equal(valid.capacityHealthPercentage, 90.0);
  assert.equal(valid.cycleCount, 150);

  // Unavailable hardware capacity metrics
  const unavailable = evaluateBatteryHealth('GOOD', null, null, null, 5);
  assert.equal(unavailable.isReliableCapacityAvailable, false);
  assert.equal(unavailable.capacityHealthPercentage, null);
  assert.ok(unavailable.explanation.includes('Battery capacity health cannot be measured reliably'));

  // Out of range glitched values (e.g. 50000mAh)
  const glitched = evaluateBatteryHealth('GOOD', 1000, 50000, 0, 5);
  assert.equal(glitched.isReliableCapacityAvailable, false);
  assert.equal(glitched.capacityHealthPercentage, null);
});

test('Backup prediction calculation and smoothing', () => {
  const baseTime = 1700000000000;
  // Discharges 10% per hour over 2 hours
  const samples: BatterySample[] = [
    { id: 1, timestamp: baseTime + 0, percentage: 70, status: 'DISCHARGING', plugType: 'NONE', temperatureCelsius: 25, voltageMv: 4000, currentMa: -300 },
    { id: 2, timestamp: baseTime + 3600_000, percentage: 60, status: 'DISCHARGING', plugType: 'NONE', temperatureCelsius: 25, voltageMv: 3900, currentMa: -300 },
    { id: 3, timestamp: baseTime + 3600_000 * 2, percentage: 50, status: 'DISCHARGING', plugType: 'NONE', temperatureCelsius: 25, voltageMv: 3800, currentMa: -300 },
  ];

  const pred = predictRemainingRuntime(60, false, samples);
  assert.ok(pred.estimatedMinutesRemaining !== null);
  // Rate ~10%/hr -> 60% remaining / 10%/hr = 6.0 hours = 360 minutes
  assert.ok(pred.estimatedMinutesRemaining >= 340 && pred.estimatedMinutesRemaining <= 380);

  // When charging, estimate pauses
  const chargingPred = predictRemainingRuntime(60, true, samples);
  assert.equal(chargingPred.estimatedMinutesRemaining, null);
  assert.equal(chargingPred.confidence, 'INSUFFICIENT_DATA');
});

test('Charging prediction with Li-ion constant voltage taper', () => {
  const baseTime = 1700000000000;
  const samples: BatterySample[] = [
    { id: 1, timestamp: baseTime + 0, percentage: 30, status: 'CHARGING', plugType: 'AC', temperatureCelsius: 30, voltageMv: 4100, currentMa: 2500 },
    { id: 2, timestamp: baseTime + 1800_000, percentage: 50, status: 'CHARGING', plugType: 'AC', temperatureCelsius: 32, voltageMv: 4200, currentMa: 2500 },
  ];

  const pred = calculateChargingPrediction(50, true, samples);
  assert.equal(pred.isEstimated, true);
  assert.equal(pred.estimatedMinutesTo80, 45); // 30% needed at 40%/hr = 0.75h = 45 min
  assert.ok(pred.estimatedMinutesToFull !== null && pred.estimatedMinutesToFull > 45);
});

test('Duplicate observation filtering', () => {
  const existing: BatterySample[] = [
    { id: 1, timestamp: 1000000, percentage: 85, status: 'DISCHARGING', plugType: 'NONE', temperatureCelsius: 26, voltageMv: 4100, currentMa: -250 },
  ];

  const duplicateSample: BatterySample = {
    id: 2,
    timestamp: 1000030, // 30 seconds later
    percentage: 85,
    status: 'DISCHARGING',
    plugType: 'NONE',
    temperatureCelsius: 26,
    voltageMv: 4100,
    currentMa: -250,
  };

  const changedSample: BatterySample = {
    id: 3,
    timestamp: 1000040,
    percentage: 84, // Changed percentage
    status: 'DISCHARGING',
    plugType: 'NONE',
    temperatureCelsius: 26,
    voltageMv: 4090,
    currentMa: -260,
  };

  assert.equal(filterDuplicateSamples(duplicateSample, existing), true);
  assert.equal(filterDuplicateSamples(changedSample, existing), false);
});
