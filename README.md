
# SCENE Platform

[![Slack Status](https://pextraslack.herokuapp.com/badge.svg)](https://pextraslack.herokuapp.com/)
[![Travis Status](https://travis-ci.org/pextralabs/scene-platform.svg?branch=development)](https://travis-ci.org/pextralabs/scene-platform)
![core](https://img.shields.io/badge/core-0.10.1-a295d6.svg)
![model](https://img.shields.io/badge/model-0.10.0-f0ad4e.svg)

*SCENE* is a platform for situation management that leverages on [**JBoss Drools**](https://github.com/droolsjbpm/drools) rule engine and its integrated Complex Event Processing features to natively support rule-based situation-awareness.

A *situation* is a complex event which endures over time as certain conditions involving relevant contexts hold. Such relevant contexts are qualified within a *role* or *part* by the very conditions held by them. Take ***John*** -  an individual whose temperature is being measured over time. Anytime ***John***'s temperature is above 37.5  it qualifies him as a ***febrile*** person.  We can refer the ***fever*** as a situation taking place with ***John*** taking the part of ***febrile*** on it.

<img src="http://i.imgur.com/wv6F1jr.png" width="600px">

The fever pattern can be described in DRL (*Drools Rule Language*) as:

```Java
declare Person
	name: String
	temperature: Double
end

declare Fever extends Situation
	febrile: Person @part @key
end

rule Fever
@role(situation)
	when
		febrile: Person(temperature > 37.5)
	then
		SituationHelper.situationDetected(drools)
end
```
In order to fully describe a SCENE situation, one must declare a **`Situation kind`** (Fever) and specify its properties. The **binding classifier** *`@part`* tags a property as a role (e.g. *febrile*) to be matched in a further situation identification. There will be conditions in which an individual fulfill a part and the **`Situation rule`** entails them as patterns (e.g. what it takes to person turn into *febrile*). A property tagged as `@part` in the class must be referred as a binding in the `situation rule` so the engine knows how to assign every individual to the right part its taking in it.

### Features
**SCENE** offers a mean to describe situation patterns with features like **generalization**, **composability**, **aggregation** and **temporal correlation** over situations.
