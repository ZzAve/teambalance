import React from "react";

const Conditional = (props: { condition: boolean; children: any }) =>
  props.condition ? props.children : <></>;

export default Conditional;
