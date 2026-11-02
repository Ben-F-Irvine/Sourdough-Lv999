# Sourdough Companion — App Plan

Nov 2, 2026 · @ben

## Overview

A personal Android app for your Samsung phone, built and installed for free, that walks you through each bake, builds the timeline from your own process, and adjusts bulk fermentation to your actual room temperature. A bake is a chain of steps with durations, so one anchor event (feeding the starter, or tapping "Done mixing") is enough to schedule everything after it. Every tool in the stack is free; the only possible cost is a thermometer if you don't already own one.

Goals for v1:

- Guide the full process, step by step, with your own notes per step
- Pick a schedule template (e.g. morning feed / overnight bulk, or evening feed / morning bake) and have the app lay out the day
- Pick a flavour, see exactly what extra ingredients you need, and add your own flavours
- One tap on "Done mixing" starts every timer that follows
- Read room temperature from a home sensor and stretch or shorten bulk to match
- Alarms that fire reliably with the screen off, with no server required for timing

## Core features

**1. Process guide.** Every step (feed starter, autolyse, mix, stretch and folds, bulk, shape, cold retard, bake) has instructions, tips and what "done" looks like. You can edit any step's text, so the guide becomes your method, not a generic one.

**2. Schedule templates.** A template is an ordered list of steps with durations and an anchor. You'd start with two:

| Template | Anchor | Shape of the day |
| --- | --- | --- |
| Morning feed, overnight bulk | Feed starter (AM) | Starter peaks midday, mix afternoon, bulk overnight, shape and bake next morning |
| Evening feed, morning bake | Feed starter (PM) | Starter peaks overnight, mix in the morning, bulk through the day, cold retard, bake |

Durations are yours to set, and you can clone a template to make variants (e.g. a warm-kitchen summer version with shorter bulk).

**3. Starter-feed trigger.** Tapping "Fed starter" logs the time and offers the matching template (it can guess AM vs PM from the clock). It then shows the projected timeline: starter peak, mix window, and the predicted bake time.

**4. Flavours.** A flavour is a named set of add-ins with amounts, plus an optional "when to add" step (e.g. at the second fold) and notes. Pick one when you start a bake and the app shows a shopping/prep checklist. Amounts can be fixed or a % of flour so they scale with your loaf. Starter set: plain, jalapeño cheddar, olive rosemary, cinnamon raisin; you add the rest.

**5. "Done mixing" button.** Starting a bake shows the mix checklist. Tapping "Done mixing" starts the real clock: every downstream timer (each fold, end of bulk, shape, retard, preheat, bake) is scheduled from that moment.

**6. Live bake screen.** Shows the current step, a countdown to the next action, and the full timeline. Each step has "Done", "+15 min" and "Skip" so you can follow the dough instead of the clock; later timers shift automatically.

## Nice-to-haves

Ranked by how much they'd help a real bake, highest first:

1. **Plan backwards from a target time.** "I want bread out of the oven Saturday 10 AM" tells you when to feed the starter. Same engine, run in reverse.
2. **Bake log.** Every bake saves its actual times, temperature trace, template, flavour, plus a photo and a 1 to 5 rating. Over time you see what worked.
3. **Recipe calculator.** Enter flour weight and hydration %; it computes water, salt and levain in grams (baker's percentages), including flavour add-ins.
4. **Starter tracker.** Feeding history, ratio used (1:1:1, 1:5:5), and a reminder if it's gone too long without a feed. The same temperature curve idea can predict starter peak.
5. **Quiet hours.** Steps that would land at 3 AM get flagged when you plan, so you can pick another template before committing.
6. **Home-screen widget.** Current step and countdown on your Samsung home screen without opening the app.
7. **Shopping list export.** Flavour ingredients plus flour/salt shared to Samsung Notes or Google Keep.

## How scheduling works

Every step's time is stored as an offset from the step before it, never as a clock time. That one rule is what makes both of your scenarios work with the same code.

&#91;embedded content: bake flow · 2 anchors, 9 steps\]

The top row is a forecast you can plan around; everything in the bottom row is a real reminder that shifts when the dough runs fast or slow.

- A bake has two anchors: **Fed starter** (sets the projected plan) and **Done mixing** (sets the live timers). Before mixing, times are projections; after, they're real reminders.
- Projected time = anchor time + sum of the durations before that step.
- When you tap "Done" early or late, or add 15 minutes, every later step re-computes from that actual time. Nothing is hard-coded to the clock.
- Steps can repeat (e.g. 4 stretch-and-folds every 30 min) by storing a count and an interval, instead of 4 separate steps.
- Some steps are "until you say so" (bulk until 50% rise). They get a target time and a reminder to check, but the next step only starts when you tap Done.
- Backward planning runs the same sum in reverse from a target finish time.

The important design choice: **timers are Android system alarms, not countdowns in the app.** The app hands each reminder to Android's AlarmManager as an exact alarm, which fires even when the phone is asleep or the app is closed. When the plan changes (you tap Done early, or the room warms up), the app cancels the affected alarms and sets new ones.

## Temperature-adjusted bulk

Bulk fermentation is scheduled by progress, not by a fixed duration: the app tracks what fraction of bulk is done given the temperatures the dough has actually sat at, and moves the end-of-bulk alarm as the room changes.

- **The curve.** You keep a small table of points: room temperature to bulk duration (e.g. 22 °C → 7 h). Between points the app interpolates in a straight line. The chart below shows suggested starting values; you edit them to match your starter and flour.
- **Progress.** Every reading adds (minutes since last reading) ÷ (bulk duration at that temperature) to progress. Bulk is predicted done when progress reaches 100%, so a cold night followed by a warm morning is handled correctly.
- **Re-planning.** After each reading the app re-computes the predicted end of bulk and re-sets the alarms for bulk end, shape, retard and bake. Changes under 10 minutes are ignored so alarms don't jitter.
- **Readings.** Every 15 minutes during bulk (Android's minimum for background work), plus one when you open the app. If the sensor can't be reached, it falls back to the last known temperature and tells you.
- **Only bulk (and optionally the starter) use it.** Cold retard is in the fridge, so it stays a fixed time.
- **Learning.** When you end bulk earlier or later than predicted, the app offers to nudge that temperature's point on the curve, so the curve becomes yours after a few bakes.

&#91;embedded content: approximate starting values for a typical 20% levain dough · 6 curve points\]

These are rough defaults for a dough with about 20% levain; a stronger or weaker starter shifts the whole curve, which is why every point is editable.

**Getting the temperature.** Two free routes, depending on what you have:

| Route | Works with | How the app reads it |
| --- | --- | --- |
| Home Assistant (recommended if you have smart-home gear) | Almost any brand: Govee, SwitchBot, Aqara, Zigbee, Ecobee, etc. | Calls Home Assistant's REST API on your Wi-Fi with a long-lived access token |
| Direct Bluetooth | BLE thermometers that broadcast readings (e.g. Govee H5075, Xiaomi LYWSD03MMC) | The phone listens for the sensor's broadcast; no hub needed, phone must be within about 10 m |
| Manual | Any thermometer | You type the temperature; the app asks every couple of hours during bulk |

Build the app against one "temperature source" interface so you can start with manual entry and plug in a sensor later without touching the scheduling code.

## Data model

Nine tables in the phone's local SQLite database (via Android's Room library) cover v1. Templates and flavours are the reusable "recipes"; a bake is one instance that copies its steps, so editing a template never changes past bakes.

| Table | Key columns | Notes |
| --- | --- | --- |
| templates | id, name, anchor\_type (feed / mix), notes | Your schedules, e.g. "Overnight bulk" |
| template\_steps | id, template\_id, order, name, instructions, duration\_min, repeat\_count, repeat\_interval\_min, wait\_for\_user (bool), temp\_adjusted (bool), phase (pre-mix / post-mix) | The heart of the app |
| flavours | id, name, notes, add\_step\_name | E.g. "Jalapeño cheddar", added at fold 2 |
| flavour\_ingredients | id, flavour\_id, name, amount, unit, is\_percent\_of\_flour | Grams or % of flour |
| temp\_curve\_points | id, curve (bulk / starter), temp\_c, duration\_min | Your editable curve |
| bakes | id, template\_id, flavour\_id, flour\_g, hydration\_pct, fed\_at, mixed\_at, finished\_at, rating, notes, photo\_path | One per loaf/batch |
| bake\_steps | id, bake\_id, order, name, planned\_at, started\_at, done\_at, progress\_pct, status | Copied from template at start; drives alarms |
| temp\_readings | id, bake\_id, read\_at, temp\_c, source | The bake's temperature trace |
| settings | key, value | Sensor source, Home Assistant URL and token, sensor ID |

## Tech stack and hosting

Recommended: a native Android app in Kotlin, installed straight onto your phone from Android Studio. It needs no server, no Play Store account and no paid service; everything runs on the phone.

| Layer | Pick | Cost | Why |
| --- | --- | --- | --- |
| Language / UI | Kotlin + Jetpack Compose | Free | Google's standard for new Android apps |
| IDE | Android Studio | Free | Builds and installs the app over USB or Wi-Fi debugging |
| Database | Room (SQLite on the phone) | Free | SQL you already know, no setup |
| Alarms | AlarmManager exact alarms + notifications | Free | Fires with the screen off; survives the app being closed |
| Background readings | WorkManager | Free | Periodic temperature checks during bulk |
| Temperature | Home Assistant REST API, or Android Bluetooth LE | Free (sensor hardware aside) | See the temperature section |
| Charts | Vico (Compose chart library) | Free | Temperature curve and bake trace |
| Distribution | Sideload the APK | Free | Publishing to the Play Store is not needed for personal use |

Samsung-specific: add the app to **Never sleeping apps** in battery settings and allow **Alarms and reminders** permission, or One UI may delay its alarms.

Alternatives: Flutter (also free, if you'd rather write Dart or want an iPhone version someday), or a web app wrapped as an Android app with Capacitor (reuses web skills, but alarms and Bluetooth need plugins and are less reliable). A localhost website alone is the weakest option, since a browser tab can't reliably wake you up.

## Build phases

Build the timer engine first and use it on a real bake before adding anything else. Each phase ends with something you can actually bake with.

1. **Alarm engine + one hard-coded template.** Data model, "Done mixing" sets exact alarms, notifications show on the lock screen. Done when: one full bake runs with every alarm on time and the screen off.
2. **Editable templates and steps.** Create, clone and reorder steps; set durations, repeats and wait-for-me steps; Done / +15 / Skip re-plans the rest. Done when: both of your schedules exist and you built them in the app.
3. **Starter-feed anchor and projected timeline.** "Fed starter" button, AM/PM template suggestion, projected bake time. Done when: feeding in the morning shows tomorrow's bake time without you doing math.
4. **Temperature.** Editable curve with chart, manual entry first, then Home Assistant or Bluetooth sensor; progress-based bulk with alarm re-planning. Done when: warming the room visibly pulls the end-of-bulk alarm earlier.
5. **Flavours.** Flavour CRUD, ingredient checklist at bake start, add-in reminder at the right step. Done when: you added a flavour of your own and it showed up mid-bake.
6. **Polish.** Bake log with temperature trace, recipe calculator, backward planning, curve learning, home-screen widget, database backup to a file.

- [ ] Install Android Studio and turn on developer options on the phone
- [ ] Check what thermometer or smart-home hub you already have
- [ ] Write down your two current schedules with real durations
- [ ] Note bulk times and room temps from your next few bakes to seed the curve

## Open questions

- Do you already have a smart thermometer or Home Assistant? That decides the temperature route.
- Where does the dough sit during bulk, and is the sensor in the same spot? A sensor across the house can be 2 to 3 °C off.
- Single loaf per bake, or do you sometimes run two flavours from one dough?
- Should anyone else in the household get the alarms? (That would need a second phone or a small server later.)
- What are your current step durations for each schedule? These become the seed data.
