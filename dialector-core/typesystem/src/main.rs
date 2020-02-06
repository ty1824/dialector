use ::std::collections::HashMap;
use ::std::hash::{Hash, Hasher};

fn main() {
    println!("Hello, world!");
}

struct TypeParameter {
    bound: dyn Type
}

trait Type {
}

#[derive(PartialEq, Eq, Hash)]
struct BooleanType {}

impl Type for BooleanType {
}

struct TopType {}

impl Type for TopType {

}

struct TypeParameterReference<'a> {
    def: &'a TypeParameter
}

impl Type for TypeParameterReference<'_> {
}