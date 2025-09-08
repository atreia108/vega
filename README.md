# Vega

[![Static Badge](https://img.shields.io/badge/Release-v1.0.0-blue?style=for-the-badge)](https://github.com/atreia108/vega/releases)
[![Static Badge](https://img.shields.io/badge/License-BSD--3-brightgreen?style=for-the-badge)](https://github.com/atreia108/vega/blob/main/LICENSE)

Vega is a lightweight software framework written in Java that lets you build distributed simulations for the [IEEE-1516 High Level Architecture](https://standards.ieee.org/ieee/1516/6687/) and the [SpaceFOM](https://www.sisostandards.org/resource/resmgr/standards_products/siso-std-018-2020_srfom.pdf) with the [Entity Component System](https://github.com/SanderMertens/ecs-faq) pattern. Below are some of the framework's features:

* Full abstraction over the SpaceFOM late-joiner initialization and executive control sequences
* Automatic handling of HLA object and interaction class publication/subscription at runtime
* Support for the Entity Component System (ECS) pattern via the [libGDX Ashley](https://www.github.com/libgdx/ashley) library
* Write all your objects and interactions as entities and let the framework manage translating data from ECS to object-oriented FOM data and vice versa

For information on how to use Vega, have a look at the tutorials on the [wiki](https://github.com/atreia108/vega/wiki) page.


## Acknowledgement

This project is a reality today thanks to product licenses and technical support from [Pitch Technologies](https://pitchtechnologies.com/) as well as the research support of the [Brunel Modeling and Simulation Group (MSG)](https://www.brunel.ac.uk/research/Groups/Modelling-and-Simulation).