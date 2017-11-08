# Equals-And-HashCode-Reloaded IntelliJ Plugin

Contains inspections:
- "Field not used in 'equals()/hashCode()' method": Will check if equals() and hashCode() implementations cover all class fields.

## How Does It Work?

The inspection is automatically enabled after installation. If a class contains a `equals()` or a `hashCode()` implementation, it will check if all instance fields have been used. Using getters is supported. Static fields are ignored, since they are equal for all instances. Final fields are ignored if they use a literal initialization, since these fields are effectively constant. Transitive fields are ignored, since they usually are calculated from other fields.  

## License

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt)
