/**
 * Provides service provider interfaces (SPI) for delegating business logic to external implementations.
 * <p>
 * This package follows the Dependency Inversion Principle by defining contracts (interfaces) for services that the core
 * gateway logic depends on, such as authorization and license validation. The actual implementations of these
 * interfaces can then be provided separately, allowing for greater flexibility and easier integration with different
 * backend services.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.provider.AuthorizeProvider}: Defines the contract for authenticating and authorizing
 * requests.</li>
 * <li>{@link org.miaixz.bus.vortex.provider.LicenseProvider}: Defines the contract for validating deployment
 * licenses.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.provider;
