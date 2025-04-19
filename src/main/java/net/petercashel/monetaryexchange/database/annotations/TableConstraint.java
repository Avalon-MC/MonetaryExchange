package net.petercashel.monetaryexchange.database.annotations;

public record TableConstraint(String ConstraintName, String LocalKey, String ForeignTable, String ForeignKey) {
}
