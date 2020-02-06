trait Type {

}

trait TypeLattice<T> {
    fn isSubtypeOf(sub: T, sup: T) -> bool;
    fn leastCommonSupertype(types: Vec<Type>) -> Type;
}