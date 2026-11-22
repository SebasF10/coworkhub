package com.coworkhub.security.config;

// Expresiones de autorización reutilizadas en @PreAuthorize (RN-13).
// Son constantes para no repetir (ni equivocarse al copiar) la misma expresión en cada método.
public final class Permisos {

    private Permisos() {
    }

    // Personal: ADMIN o RECEPCION
    public static final String STAFF = "hasAnyRole('ADMIN','RECEPCION')";

    // Personal, o el propio miembro cuyo id llega en la URL (#id)
    public static final String STAFF_O_MIEMBRO_DEL_ID =
            STAFF + " or @autorizacion.esElMiembro(authentication, #id)";

    // Personal, o el dueño de la reserva cuyo id llega en la URL (#id)
    public static final String STAFF_O_DUENO_DE_LA_RESERVA =
            STAFF + " or @autorizacion.esDuenoDeLaReserva(authentication, #id)";

    // Personal, o un miembro que reserva a su propio nombre (miembroId del cuerpo de la solicitud)
    public static final String STAFF_O_MIEMBRO_DE_LA_SOLICITUD =
            STAFF + " or @autorizacion.esElMiembro(authentication, #solicitud.miembroId())";

    // Personal, o un miembro que filtra por su propio miembroId (parámetro de la URL)
    public static final String STAFF_O_FILTRO_DE_MIEMBRO =
            STAFF + " or (#miembroId != null and @autorizacion.esElMiembro(authentication, #miembroId))";
}