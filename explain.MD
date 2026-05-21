# Why `gotoDeliverySpot` Was Not Activated

## Summary

The `gotoDeliverySpot` rule was not activating because the LIDA pipeline had a mismatch between the procedural rule and the attention codelet that was supposed to broadcast its condition.

In `configs/Agent.xml`, the procedural rule should activate only when the leaflet is complete and the agent sees the delivery spot:

```xml
<param name="scheme.5">if complete and see delivery spot, goto deliveryspot|(deliverySpot,completeLeaflet)()|action.gotoDeliverySpot|()()|0.4</param>
```

That means procedural memory needs both `deliverySpot` and `completeLeaflet` to be broadcast through the Global Workspace.

However, the attention codelet responsible for broadcasting delivery-related information was still configured like this:

```xml
<param name="nodes" type="string">deliverySpot,completeLeaflet,nearDeliverySpot</param>
```

For `BasicAttentionCodelet`, listing multiple nodes means "only create a coalition if all of these nodes are present at the same time". It does not mean "broadcast any of these nodes". So the movement rule and delivery rule need separate attention patterns.

The movement rule needs `deliverySpot,completeLeaflet`: complete leaflet and visible delivery spot. The delivery rule needs `nearDeliverySpot,completeLeaflet`: complete leaflet and close enough to deliver.

## LIDA Activation Path

The relevant path is:

1. `Environment` reads the WS3D world state.
2. `SensoryMemory` copies relevant values from `Environment`.
3. Detectors activate PAM nodes, such as `deliverySpot`.
4. The Workspace receives perceptual content from PAM.
5. Attention codelets look for configured node patterns in the Workspace.
6. If an attention codelet finds its sought content, it creates a coalition in the Global Workspace.
7. Procedural Memory receives broadcasts from the Global Workspace.
8. If a scheme's context conditions are present, it creates a behavior.
9. Action Selection chooses a behavior.
10. Sensory Motor Memory sends the action to `Environment.processAction`.
11. `Environment.performAction` executes movement in WS3D.

The detector was not the main problem. The important blockage was between Workspace and Global Workspace, inside the attention codelet configuration.

## The Broken Configuration

Before the fix, delivery-related attention was configured as one combined pattern:

```xml
<task name="DeliverySpotAttentionCodelet">
    <tasktype>BasicAttentionCodelet</tasktype>
    <ticksperrun>5</ticksperrun>
    <param name="nodes" type="string">deliverySpot,completeLeaflet,nearDeliverySpot</param>
    ...
</task>
```

This created a strict requirement:

- `deliverySpot` must be active.
- `completeLeaflet` must be active.
- `nearDeliverySpot` must be active.

Only then would the coalition be built.

That conflicts with the movement rule:

```xml
<param name="scheme.5">if complete and see delivery spot, goto deliveryspot|(deliverySpot,completeLeaflet)()|action.gotoDeliverySpot|()()|0.4</param>
```

The movement rule needs the agent to see the delivery spot while holding a completed leaflet. It should not also require `nearDeliverySpot`, because the purpose of this rule is to move toward the delivery spot before the agent is near it.

## The Fix

The attention configuration was split into two attention codelets.

The first one broadcasts the condition for moving to the delivery spot:

```xml
<task name="DeliverySpotAttentionCodelet">
    <tasktype>BasicAttentionCodelet</tasktype>
    <ticksperrun>5</ticksperrun>
    <param name="nodes" type="string">deliverySpot,completeLeaflet</param>
    ...
</task>
```

This lets `scheme.5` activate only when the agent sees the delivery spot and has a completed leaflet.

The second one handles the delivery-ready condition:

```xml
<task name="DeliveryReadyAttentionCodelet">
    <tasktype>BasicAttentionCodelet</tasktype>
    <ticksperrun>5</ticksperrun>
    <param name="nodes" type="string">completeLeaflet,nearDeliverySpot</param>
    ...
</task>
```

This keeps the delivery behavior separate:

```xml
<param name="scheme.6">if complete and near delivery spot, deliver leaflet|(nearDeliverySpot,completeLeaflet)()|action.deliverLeaflet|()()|0.5</param>
```

So the intended behavior becomes:

1. If the agent has a complete leaflet and sees the delivery spot, `deliverySpot,completeLeaflet` can be broadcast.
2. Procedural memory can instantiate `gotoDeliverySpot`.
3. The agent walks toward the delivery spot.
4. When it is near the delivery spot and has a complete leaflet, `completeLeaflet,nearDeliverySpot` can be broadcast.
5. Procedural memory can instantiate `deliverLeaflet`.

## Another Configuration Issue

There was also a source/resource mismatch in `factoryData.xml`.

The runtime `configs/factoryData.xml` already defined these task types:

```xml
<task name="DeliverySpotDetector">
<task name="CompleteLeafletDetector">
<task name="NearDeliverySpotDetector">
```

But `src/main/resources/configs/factoryData.xml` did not define them. That means future builds or copied resource configs could lose those detector definitions.

I added those missing task definitions to:

```text
src/main/resources/configs/factoryData.xml
bin/main/configs/factoryData.xml
```

The root runtime file already had them.

## Files Changed

The attention-codelet fix was applied to:

```text
configs/Agent.xml
src/main/resources/configs/Agent.xml
bin/main/configs/Agent.xml
```

The detector factory sync was applied to:

```text
src/main/resources/configs/factoryData.xml
bin/main/configs/factoryData.xml
```

## Verification

The project builds successfully:

```bash
./gradlew build
```

I did not run the full WS3D simulation because that requires starting the Docker-based world server with:

```bash
./ws3d.sh
```

Then, in another terminal:

```bash
./gradlew run
```

During a manual run, useful signs to watch for are:

```text
Delivery Spot detected at (...)
Action: gotoDeliverySpot
Attempting to go to delivery spot...
```

If those appear, the detection, attention broadcast, procedural rule, and action path are connected.
