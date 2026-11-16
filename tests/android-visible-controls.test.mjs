import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';

const base=new URL('../android/app/src/main/',import.meta.url);
const source=readFileSync(new URL('java/br/com/joaovictor/meuritmolite/OnlineActivity.java',base),'utf8');
const theme=readFileSync(new URL('res/values/styles.xml',base),'utf8');

test('native controls remain visible even when the Android theme hides ActionBar',()=>{
 assert.match(theme,/Theme\.Material\.Light\.NoActionBar/);
 assert.match(source,/Button alarmButton=new Button\(this\)/);
 assert.match(source,/alarmButton\.setText\("Alarmes do aparelho"\)/);
 assert.match(source,/alarmButton\.setOnClickListener\(v->startActivity\(new Intent\(this,MainActivity\.class\)\)\)/);
 assert.match(source,/refreshButton\.setOnClickListener\(v->web\.reload\(\)\)/);
 assert.match(source,/root\.addView\(controls,new LinearLayout\.LayoutParams\(-1,-2\)\)/);
});
